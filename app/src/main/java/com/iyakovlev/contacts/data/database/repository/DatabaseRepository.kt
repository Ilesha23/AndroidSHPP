package com.iyakovlev.contacts.data.database.repository

import com.iyakovlev.contacts.data.database.Database
import com.iyakovlev.contacts.data.database.entities.ContactEntity
import com.iyakovlev.contacts.data.database.entities.UserEntity
import com.iyakovlev.contacts.data.database.interfaces.ContactDao
import com.iyakovlev.contacts.data.database.interfaces.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DatabaseRepository @Inject constructor(
    private val database: Database
) : ContactDao, UserDao {

    private val contactDao = database.getContactsDao()
    private val userDao = database.getUsersDao()

    override suspend fun insertContacts(list: List<ContactEntity>) {
        withContext(Dispatchers.IO) {
            contactDao.insertContacts(list)
        }
    }

    override suspend fun getContacts(): List<ContactEntity> {
        return withContext(Dispatchers.IO) {
            return@withContext contactDao.getContacts()
        }
    }

    override suspend fun insert(contact: ContactEntity) {
        withContext(Dispatchers.IO) {
            contactDao.insert(contact)
        }
    }

    override suspend fun delete(id: Long) {
        withContext(Dispatchers.IO) {
            contactDao.delete(id)
        }
    }

    override suspend fun insertUsers(users: List<UserEntity>) {
        withContext(Dispatchers.IO) {
            userDao.insertUsers(users)
        }
    }

    override suspend fun getUsers(): List<UserEntity> {
        return withContext(Dispatchers.IO) {
            return@withContext userDao.getUsers()
        }
    }

    override suspend fun getUser(id: Long): ContactEntity {
        return withContext(Dispatchers.IO) {
            return@withContext userDao.getUser(id)
        }
    }


}