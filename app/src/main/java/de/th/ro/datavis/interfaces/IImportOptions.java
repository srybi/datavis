package de.th.ro.datavis.interfaces;

import android.text.TextWatcher;

public interface IImportOptions {

    TextWatcher descriptionChanged();

    void insertNewConfig();

    void chooseExistingConfig();

    void addImportAntenna();

    void addDefaultAntenna();

    void addMetaData();

    void addMetaDataFolder();

    void addFFS();

}
