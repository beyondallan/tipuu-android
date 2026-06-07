package tech.ti.social.feature.blufi.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tech.ti.social.feature.blufi.data.bluetooth.BleScanner
import tech.ti.social.feature.blufi.data.bluetooth.BlufiDeviceConnector
import tech.ti.social.feature.blufi.data.bluetooth.BlufiFrameEncoder
import tech.ti.social.feature.blufi.data.crypto.AesEncryptor
import tech.ti.social.feature.blufi.data.crypto.DhKeyExchange
import tech.ti.social.feature.blufi.data.protocol.BlufiProtocol
import tech.ti.social.feature.blufi.domain.BlufiRepository
import tech.ti.social.feature.blufi.domain.BlufiRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BlufiModule {

    @Provides
    @Singleton
    fun provideBlufiFrameEncoder(): BlufiFrameEncoder = BlufiFrameEncoder()

    @Provides
    @Singleton
    fun provideDhKeyExchange(): DhKeyExchange = DhKeyExchange()

    @Provides
    @Singleton
    fun provideAesEncryptor(): AesEncryptor = AesEncryptor()

    @Provides
    @Singleton
    fun provideBleScanner(@ApplicationContext context: Context): BleScanner = BleScanner(context)

    @Provides
    @Singleton
    fun provideBlufiDeviceConnector(
        @ApplicationContext context: Context,
        frameEncoder: BlufiFrameEncoder
    ): BlufiDeviceConnector = BlufiDeviceConnector(context, frameEncoder)

    @Provides
    @Singleton
    fun provideBlufiProtocol(
        connector: BlufiDeviceConnector,
        frameEncoder: BlufiFrameEncoder,
        dhKeyExchange: DhKeyExchange,
        aesEncryptor: AesEncryptor
    ): BlufiProtocol = BlufiProtocol(connector, frameEncoder, dhKeyExchange, aesEncryptor)

    @Provides
    @Singleton
    fun provideBlufiRepository(
        bleScanner: BleScanner,
        connector: BlufiDeviceConnector,
        protocol: BlufiProtocol
    ): BlufiRepository = BlufiRepositoryImpl(bleScanner, connector, protocol)
}
