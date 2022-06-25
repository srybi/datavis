package de.th.ro.datavis.ui.bottomSheet;

import de.th.ro.datavis.ar.IObserver;

public interface ISubject {
    void subscribe(IObserver observer);
}
