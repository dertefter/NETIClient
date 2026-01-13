package com.dertefter.neticore.features.authorization

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dertefter.neticore.features.authorization.model.AuthStatusType
import com.dertefter.neticore.features.authorization.model.User
import com.dertefter.neticore.local.CoreDataStoreManager
import com.dertefter.neticore.local.UserDataStoreManager
import com.dertefter.neticore.network.NetworkClient
import com.dertefter.neticore.network.methods.CiuMethods
import com.dertefter.neticore.network.methods.Login2Methods
import com.dertefter.neticore.network.methods.MobileMethods
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.concurrent.atomic.AtomicBoolean


class AuthorizationFeature(
    val client: NetworkClient,
    val userDataStoreManager: UserDataStoreManager,
    val coreDataStoreManager: CoreDataStoreManager
) {

    private val isBusy = AtomicBoolean(false)
    private val gson = Gson()
    private val SAVED_USERS_KEY = stringPreferencesKey("saved_users")
    private val CURRENT_USER_KEY = stringPreferencesKey("current_user")

    val ciuStatus = MutableStateFlow(AuthStatusType.LOADING)
    val mobileStatus = MutableStateFlow(AuthStatusType.LOADING)

    val currentUser: Flow<User?> = coreDataStoreManager.dataStore.data
        .map { prefs ->
            prefs[CURRENT_USER_KEY]?.let { json ->
                try {
                    gson.fromJson(json, User::class.java)
                } catch (e: Exception) {
                    null
                }
            }
        }

    val savedUsersList: Flow<List<User>> = coreDataStoreManager.dataStore.data
        .map { prefs ->
            val json = prefs[SAVED_USERS_KEY] ?: "[]"
            try {
                val type = object : TypeToken<List<User>>() {}.type
                gson.fromJson<List<User>>(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

    private val login2Methods = Login2Methods(client.login2ApiService)
    private val mobileMethods = MobileMethods(client.mobileApiService)
    val ciuMethods = CiuMethods(client.ciuApiService)

    suspend fun login(user: User, clearIfError: Boolean = false) {
        if (!isBusy.compareAndSet(false, true)) return

        try {
            client.clearSession()
            userDataStoreManager.switchToUser(user.login)
            setCurrentUser(user)
            ciuStatus.value = AuthStatusType.LOADING
            mobileStatus.value = AuthStatusType.LOADING
            login2Methods.authUser(user.login, user.password)
            val userDetail = ciuMethods.fetchUserDetail()
            if (userDetail != null && !userDetail.name.isNullOrEmpty()) {
                ciuStatus.value = AuthStatusType.AUTHORIZED
                addUserToList(user)
            } else {
                ciuStatus.value = AuthStatusType.AUTHORIZED_WITH_ERROR
                if (clearIfError) {
                    doLogoutWork()
                    return
                }
            }
            val params = HashMap<String?, String?>()
            params["X-Username"] = user.login
            params["X-Password"] = user.password
            val mobileAuthStatus = mobileMethods.mobileLogin(params)
            if (mobileAuthStatus == true) {
                mobileStatus.value = AuthStatusType.AUTHORIZED
            } else if (mobileAuthStatus == false) {
                mobileStatus.value = AuthStatusType.UNAUTHORIZED
            } else {
                mobileStatus.value = AuthStatusType.AUTHORIZED_WITH_ERROR
            }
        } finally {
            isBusy.set(false)
        }
    }

    suspend fun logout() {
        if (!isBusy.compareAndSet(false, true)) return

        try {
            doLogoutWork()
        } finally {
            isBusy.set(false)
        }
    }

    private suspend fun doLogoutWork() {
        client.clearSession()
        userDataStoreManager.resetToGuest()
        setCurrentUser(null)
        ciuStatus.value = AuthStatusType.UNAUTHORIZED
        mobileStatus.value = AuthStatusType.UNAUTHORIZED
    }

    suspend fun setCurrentUser(user: User?) {
        coreDataStoreManager.dataStore.edit { prefs ->
            if (user == null) {
                prefs.remove(CURRENT_USER_KEY)
            } else {
                prefs[CURRENT_USER_KEY] = gson.toJson(user)
            }
        }
    }

    suspend fun addUserToList(user: User) {
        coreDataStoreManager.dataStore.edit { prefs ->
            val json = prefs[SAVED_USERS_KEY] ?: "[]"
            val type = object : TypeToken<MutableList<User>>() {}.type
            val users: MutableList<User> = gson.fromJson(json, type) ?: mutableListOf()

            if (users.none { it.login == user.login }) {
                users.add(user)
            }
            prefs[SAVED_USERS_KEY] = gson.toJson(users)
        }
    }

    suspend fun removeUserFromList(login: String) {
        coreDataStoreManager.dataStore.edit { prefs ->
            val json = prefs[SAVED_USERS_KEY] ?: "[]"
            val type = object : TypeToken<MutableList<User>>() {}.type
            val users: MutableList<User> = gson.fromJson(json, type) ?: mutableListOf()

            prefs[SAVED_USERS_KEY] = gson.toJson(users.filter { it.login != login })
        }
    }
}
