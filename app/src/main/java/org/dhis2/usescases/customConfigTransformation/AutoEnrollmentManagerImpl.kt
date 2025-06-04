package org.dhis2.usescases.customConfigTransformation
import com.google.gson.Gson
import io.reactivex.Flowable
import org.hisp.dhis.android.core.D2

class AutoEnrollmentManagerImpl(private val d2: D2): AutoEnrollmentManager {

    override fun getCustomConfigurations(): Flowable<AutoEnrollmentConfigurations> {
        val  configEntry = d2.dataStoreModule()
            .dataStore().byNamespace()
            .eq("programMapping")
            .byKey().eq("")
            .one().blockingGet()
        return if(configEntry !=null){
            d2.dataStoreModule().dataStore().byNamespace().eq("programMapping")
                .byKey().eq("mapping_rules").one().get()
                .toFlowable().map {
                    Gson().fromJson(
                        it.value(),
                        AutoEnrollmentConfigurations::class.java
                    )
                }
        } else Flowable.just(
            Gson().fromJson(
                AutoEnrollmentConfigurations.createDefaultEnrollmentConfigObject(),
                AutoEnrollmentConfigurations::class.java
            )
        )
    }
}