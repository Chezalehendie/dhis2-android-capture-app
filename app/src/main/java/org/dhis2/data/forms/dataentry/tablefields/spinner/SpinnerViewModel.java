package org.dhis2.data.forms.dataentry.tablefields.spinner;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.dhis2.composetable.model.DropdownOption;
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;

import java.util.List;

@AutoValue
public abstract class SpinnerViewModel extends FieldViewModel {

    @NonNull
    public abstract String hint();

    @NonNull
    public abstract String optionSet();

    public static SpinnerViewModel create(String id, String label, String hintFilterOptions, Boolean mandatory, String optionSet, String value, String section, Boolean editable, String description, String dataElement, List<String> listCategoryOption, String storeBy, int row, int column, String categoryOptionCombo, String catCombo, List<DropdownOption> options) {
        return new AutoValue_SpinnerViewModel(id, label, mandatory, value, section, null, editable,null,null, description,dataElement,listCategoryOption, options, storeBy, row, column, categoryOptionCombo,catCombo, hintFilterOptions, optionSet);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_SpinnerViewModel(uid(),label(),true,value(),programStageSection(),allowFutureDate(),editable(),warning(),error(),description(),dataElement(), options(), optionsList(), storeBy(), row(), column(), categoryOptionCombo(),catCombo(), hint(),optionSet());
    }

    @Override
    public FieldViewModel setValue(String value) {
        return new AutoValue_SpinnerViewModel(uid(),label(),mandatory(),value,programStageSection(),allowFutureDate(),editable(),warning(),error(),description(),dataElement(), options(), optionsList(), storeBy(), row(), column(), categoryOptionCombo(),catCombo(), hint(),optionSet());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_SpinnerViewModel(uid(),label(),mandatory(),value(),programStageSection(),allowFutureDate(),editable(),warning(),error,description(),dataElement(), options(), optionsList(), storeBy(), row(), column(), categoryOptionCombo(),catCombo(), hint(),optionSet());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_SpinnerViewModel(uid(),label(),mandatory(),value(),programStageSection(),allowFutureDate(),editable(),warning,error(),description(),dataElement(), options(), optionsList(), storeBy(), row(), column(), categoryOptionCombo(),catCombo(), hint(),optionSet());
    }

   @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_SpinnerViewModel(uid(),label(),mandatory(),data,programStageSection(),allowFutureDate(),editable(),warning(),error(),description(),dataElement(), options(), optionsList(), storeBy(), row(), column(),categoryOptionCombo(), catCombo(),hint(),optionSet());
    }
}
