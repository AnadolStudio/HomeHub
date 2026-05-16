package com.anadolstudio.template.feature.lightDetail.di

import com.anadolstudio.template.di.viewmodel.ViewModelFactoryKey
import com.anadolstudio.template.feature.lightDetail.presentation.LightDetailViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface LightDetailModule {

    @Binds
    @IntoMap
    @ViewModelFactoryKey(LightDetailViewModel.Factory::class)
    fun bindLightDetailViewModelFactory(factory: LightDetailViewModel.Factory): Any
}
