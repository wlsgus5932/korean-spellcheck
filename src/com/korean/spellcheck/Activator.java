package com.korean.spellcheck;

import static com.korean.spellcheck.log.Log.logInfo;
import org.eclipse.ui.IStartup;

public class Activator implements IStartup {
    @Override
    public void earlyStartup() {
    	logInfo("Started Spell Checker Plugin");
    }
}
