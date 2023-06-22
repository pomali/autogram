package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.Main;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.*;
import digital.slovensko.autogram.core.errors.*;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.UI;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class CliUI implements UI {
    SigningKey activeKey;

    @Override
    public void startSigning(SigningJob job, Autogram autogram) {
        System.out.println("Starting signing file " + job.getDocument().getName());
        if (activeKey == null) {
            autogram.pickSigningKeyAndThen(key -> {
                activeKey = key;
                autogram.sign(job, activeKey);
            });
        } else {
            autogram.sign(job, activeKey);
        }

    }

    @Override
    public void pickTokenDriverAndThen(List<TokenDriver> drivers, Consumer<TokenDriver> callback) {
        TokenDriver pickedDriver;
        if (drivers.isEmpty()) {
            showError(new NoDriversDetectedException());
            return;
        } else if (drivers.size() == 1) {
            pickedDriver = drivers.get(0);
        } else {
            var i = new AtomicInteger(1);
            System.out.println("Pick driver:");
            drivers.forEach(driver -> {
                System.out.print("[" + i + "] ");
                System.out.println(driver.getName());
                i.addAndGet(1);
            });
            pickedDriver = drivers.get(CliUtils.readInteger() - 1);
        }
        callback.accept(pickedDriver);
    }

    @Override
    public void requestPasswordAndThen(TokenDriver driver, Consumer<char[]> callback) {
        if (!driver.needsPassword()) {
            callback.accept(null);
            return;
        }
        System.out.println("Enter security code for driver:");
        callback.accept(CliUtils.readLine()); // TODO do not show pin
    }

    @Override
    public void pickKeyAndThen(List<DSSPrivateKeyEntry> keys, Consumer<DSSPrivateKeyEntry> callback) {
        if (keys.isEmpty()) {
            showError(new NoKeysDetectedException());
            return;
        }

        callback.accept(keys.get(0));
    }

    @Override
    public void onWorkThreadDo(Runnable callback) {
        callback.run(); // no threads
    }

    @Override
    public void onUIThreadDo(Runnable callback) {
        callback.run(); // no threads
    }

    @Override
    public void onSigningSuccess(SigningJob job) {

    }

    @Override
    public void onSigningFailed(AutogramException e) {
        throw e;
    }

    @Override
    public void onDocumentSaved(File file) {
        System.out.println("File successfully signed. Signed file saved as " + file.getName() + " in " + file.getParent());
    }

    @Override
    public void onPickSigningKeyFailed(AutogramException e) {
        showError(e);
    }

    @Override
    public void onUpdateAvailable() {
        System.out.println("Nová verzia");
        System.out.println(String.format("Je dostupná nová verzia a odporúčame stiahnuť aktualizáciu. Najnovšiu verziu si možete vždy stiahnuť na %s.", Updater.LATEST_RELEASE_URL));
    }

    @Override
    public void onAboutInfo() {
        System.out.println("""
        O projekte Autogram
        Autogram je jednoduchý nástroj na podpisovanie podľa európskej regulácie eIDAS, slovenských zákonov a štandardov. Môžete ho používať komerčne aj nekomerčne a úplne zadarmo.
        Autori a sponzori
        Autormi tohto projektu sú Jakub Ďuraš, Slovensko.Digital, CRYSTAL CONSULTING, s.r.o, Solver IT s.r.o. a ďalší spoluautori.
        Licencia a zdrojové kódy
        Tento softvér pôvodne vychádza projektu z Octosign White Label od Jakuba Ďuraša, ktorý je licencovaný pod MIT licenciou. So súhlasom autora je táto verzia distribuovaná pod licenciou EUPL v1.2.
        Zdrojové kódy sú dostupné na https://github.com/slovensko-digital/autogram.""");
        System.out.println(String.format("Verzia: %s", Main.getVersion()));
    }

    @Override
    public void onPDFAComplianceCheckFailed(SigningJob job) {
        throw new PDFAComplianceException();
    }

    public void showError(AutogramException e) {
        String errMessage = "";
        if (e instanceof FunctionCanceledException) {
            errMessage = "No security code entered";
        } else if (e instanceof InitializationFailedException) {
            errMessage = "Unable to read card";
        } else if (e instanceof NoDriversDetectedException) {
            errMessage = "No available drivers found";
        } else if (e instanceof NoKeysDetectedException) {
            errMessage = "No signing keys found";
        } else if (e instanceof PDFAComplianceException) {
            errMessage = "Document is not PDF/A compliant";
        } else if (e instanceof PINIncorrectException) {
            errMessage = "Incorrect security code";
        } else if (e instanceof PINLockedException) {
            errMessage = "PIN is blocked";
        } else if (e instanceof SigningCanceledByUserException) {
            errMessage = "Signing canceled by user";
        } else if (e instanceof SigningWithExpiredCertificateException) {
            errMessage = "Signing with expired certificate";
        } else if (e instanceof TokenNotRecognizedException) {
            errMessage = "Token not recognized";
        } else if (e instanceof TokenRemovedException) {
            errMessage = "Token removed";
        } else {
            errMessage = "Unknown error occurred";
        }
        System.err.println(errMessage);
    }
}
