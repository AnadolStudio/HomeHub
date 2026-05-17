package com.anadolstudio.template.feature.sceneCreate.presentation

/**
 * Ключ savedStateHandle: список сущностей устройства ДО открытия DeviceDetail.
 * Пишется на стороне picker'а (перед навигацией) в SceneCreate's savedStateHandle.
 * SceneCreate читает через ObserveResultValue, сохраняет в VM. На onDeviceConfigured
 * сравнивает новое состояние со снимком — в draft попадают только различающиеся сущности.
 */
internal const val SCENE_DEVICE_SNAPSHOT_KEY: String = "sceneDeviceSnapshot"

/**
 * Ключ savedStateHandle: маркер "перезагрузить список сцен". Ставится на dispose SceneCreate
 * в previousBackStackEntry (AutomationList) — там читается через ObserveResultValue и триггерит
 * перезапрос состояний. WS subscribeToStateChangedEvents не подхватит НОВУЮ сущность сцены
 * (он только обновляет уже существующие в списке), поэтому нужен явный реload.
 */
internal const val SCENE_LIST_NEEDS_REFRESH_KEY: String = "sceneListNeedsRefresh"
