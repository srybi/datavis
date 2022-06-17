package de.th.ro.datavis.interfaces;

import android.text.TextWatcher;

public interface IImportOptions {

    TextWatcher descriptionChanged();

    void addImportAntenna();

    void addDefaultAntenna();

    void addMetaData();

    void addMetaDataFolder();

    void addFFS();

}
