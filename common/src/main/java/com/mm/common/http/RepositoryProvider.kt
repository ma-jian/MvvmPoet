package com.mm.common.http

import androidx.annotation.MainThread
import com.mm.common.base.BaseRepository
import com.mm.http.RetrofitCache
import kotlin.reflect.KClass

/**
 * Created by : m
 * @since 1.0
 */

class RepositoryProvider {

    class ServiceLazy<S : Any>(
        private val serviceClass: KClass<S>,
        private val factoryPromise: () -> RetrofitCache,
    ) : Lazy<S> {
        private var cached: S? = null
        override val value: S
            get() {
                val service = cached
                return if (service == null) {
                    val retrofit = factoryPromise()
                    retrofit.create(serviceClass.java).also {
                        cached = it
                    }
                } else {
                    service
                }
            }

        override fun isInitialized(): Boolean = cached != null
    }

    class RepositoryLazy<R : BaseRepository>(
        private val repositoryClass: KClass<R>,
        private val factoryPromise: () -> Factory,
    ) : Lazy<R> {
        private var cached: R? = null
        override val value: R
            get() {
                val repository = cached
                return if (repository == null) {
                    val repositoryFactory = factoryPromise()
                    return repositoryFactory.create(repositoryClass.java).also {
                        cached = it
                    }
                } else {
                    repository
                }
            }

        override fun isInitialized(): Boolean = cached != null
    }


    interface Factory {
        fun <T : BaseRepository> create(clazz: Class<T>): T
    }

    open class NewInstanceFactory : Factory {
        companion object {
            private var sInstance: NewInstanceFactory? = null

            /**
             * Retrieve a singleton instance of NewInstanceFactory.
             *
             * @return A valid [NewInstanceFactory]
             */
            val instance: NewInstanceFactory
                get() {
                    return sInstance ?: NewInstanceFactory().also {
                        sInstance = it
                    }
                }
        }

        override fun <T : BaseRepository> create(clazz: Class<T>): T {
            return try {
                clazz.newInstance()
            } catch (e: InstantiationException) {
                throw RuntimeException("Cannot create an instance of $clazz", e)
            } catch (e: IllegalAccessException) {
                throw RuntimeException("Cannot create an instance of $clazz", e)
            }
        }
    }
}

/**
 * Service 接口工厂
 */
@MainThread
inline fun <reified S : Any> BaseRepository.serviceFactory(
    noinline retrofitCache: (() -> RetrofitCache)? = null,
): Lazy<S> {
    val factoryPromise = retrofitCache ?: {
        BaseRepository.defaultRetrofitProviderFactory()
    }
    return RepositoryProvider.ServiceLazy(S::class, factoryPromise)
}

/**
 * Repository 工厂
 */
@MainThread
inline fun <reified R : BaseRepository> repositoryFactory(
    noinline factoryProducer: (() -> RepositoryProvider.Factory)? = null
): Lazy<R> {
    val factoryPromise = factoryProducer ?: {
        RepositoryProvider.NewInstanceFactory.instance
    }

    return RepositoryProvider.RepositoryLazy(R::class, factoryPromise)
}