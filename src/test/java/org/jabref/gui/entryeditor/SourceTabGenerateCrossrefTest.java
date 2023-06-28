package org.jabref.gui.entryeditor;

import org.jabref.gui.DialogService;
import org.jabref.gui.StateManager;
import org.jabref.gui.keyboard.KeyBindingRepository;
import org.jabref.gui.undo.CountingUndoManager;
import org.jabref.logic.bibtex.FieldPreferences;
import org.jabref.logic.importer.ImportFormatPreferences;
import org.jabref.model.database.BibDatabase;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.field.InternalField;
import org.jabref.model.entry.field.StandardField;
import org.jabref.model.entry.field.UnknownField;
import org.jabref.model.entry.types.StandardEntryType;
import org.jabref.model.util.DummyFileUpdateMonitor;
import org.jabref.testutils.category.GUITest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.testfx.framework.junit5.ApplicationExtension;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

import static org.mockito.Mockito.mock;

@GUITest
@ExtendWith(ApplicationExtension.class)
public class SourceTabGenerateCrossrefTest {

    private SourceTab sourceTab;
    private BibDatabase database;


    @BeforeEach
    public void mockSourceTab(){
        database = new BibDatabase();
        BibDatabaseContext bibDatabaseContext = new BibDatabaseContext(database);
        StateManager stateManager = new StateManager();
        stateManager.setActiveDatabase(bibDatabaseContext);

        KeyBindingRepository keyBindingRepository = new KeyBindingRepository(Collections.emptyList(), Collections.emptyList());

        sourceTab = new SourceTab(
                new BibDatabaseContext(),
                new CountingUndoManager(),
                mock(FieldPreferences.class),
                mock(ImportFormatPreferences.class, Answers.RETURNS_DEEP_STUBS),
                new DummyFileUpdateMonitor(),
                mock(DialogService.class),
                stateManager,
                keyBindingRepository);
    }


    @Test
    void testCreateCrossref(){
        BibEntry inproceedings = createInproceedingsWithUtilFields();

        String keyCreatedProceeding = "proc-" + inproceedings.getField(StandardField.TITLE).get();
        keyCreatedProceeding = keyCreatedProceeding.replace(" ", "_");

        database.insertEntry(inproceedings);

        sourceTab.createProceedingCrossref(inproceedings);

        BibEntry expectedEntry = createExpectedProceedings(keyCreatedProceeding);

        if(database.getEntryByCitationKey(keyCreatedProceeding).isPresent()){
            BibEntry createdEntry = database.getEntryByCitationKey(keyCreatedProceeding).get();

            assertEquals(expectedEntry, createdEntry);
        }else{
            fail("Entrada não foi criada com sucesso");
        }
    }

    @Test
    void testTryCreateProceedingWithDuplicateValue(){
        BibEntry inproceedings = createInproceedingsWithUtilFields();

        String keyCreatedProceeding = "proc-" + inproceedings.getField(StandardField.TITLE).get();
        keyCreatedProceeding = keyCreatedProceeding.replace(" ", "_");

        BibEntry proceedings = createExpectedProceedings(keyCreatedProceeding);
        database.insertEntries(inproceedings, proceedings);

        sourceTab.createProceedingCrossref(inproceedings);

        // check if exists only 2 entries in database(inproceedings and proceedings that we already add to test)
        assertEquals(database.getEntryCount(), 2);
    }

    @Test
    void testCreateInproceedingsEmpty(){
        BibEntry inproceedings = new BibEntry(StandardEntryType.InProceedings);

        database.insertEntry(inproceedings);

        sourceTab.createProceedingCrossref(inproceedings);

        // verify if exists only one entry(the empty inproceedings entry)
        assertEquals(database.getEntryCount(), 1);
    }

    @Test
    void testCreateInproceedingsWithoutUtilFields(){

        BibEntry inproceedings = new BibEntry(StandardEntryType.InProceedings);
        inproceedings.setField(StandardField.AUTHOR, "Ferber, Jacques");
        inproceedings.setField(StandardField.ISBN, "1-234-5678-9");

        database.insertEntry(inproceedings);

        sourceTab.createProceedingCrossref(inproceedings);

        // verify if exists only one entry(the inproceedings entry without util fields to extract)
        assertEquals(database.getEntryCount(), 1);

    }


    private static BibEntry createExpectedProceedings(String keyCreatedProceeding) {
        BibEntry expectedEntry = new BibEntry(StandardEntryType.Proceedings);
        expectedEntry.setField(StandardField.ADDRESS, "Frankfurt");
        expectedEntry.setField(StandardField.ORGANIZATION, "INTERNATIONAL STUDY CONFERENCE ON CLASSIFICATION RESEARCH");
        expectedEntry.setField(StandardField.PUBLISHER, "Indeks Verlag");
        expectedEntry.setField(StandardField.TITLE, "Repensando os conceitos no estudo de classificacão");
        expectedEntry.setField(StandardField.BOOKTITLE, "Proceedings...");
        expectedEntry.setField(StandardField.YEAR, "1982");
        expectedEntry.setField(new UnknownField("conference-year"), "1982");
        expectedEntry.setField(new UnknownField("conference-location"), "Augsburg. Universal Classification: Subject analysis and ordering systems");
        expectedEntry.setField(InternalField.KEY_FIELD, keyCreatedProceeding);
        return expectedEntry;
    }

    private static BibEntry createInproceedingsWithUtilFields() {
        BibEntry inproceedings = new BibEntry(StandardEntryType.InProceedings);
        inproceedings.setField(StandardField.AUTHOR, "Kaula, P. N");
        inproceedings.setField(StandardField.ADDRESS, "Frankfurt");
        inproceedings.setField(StandardField.ORGANIZATION, "INTERNATIONAL STUDY CONFERENCE ON CLASSIFICATION RESEARCH");
        inproceedings.setField(StandardField.PUBLISHER, "Indeks Verlag");
        inproceedings.setField(StandardField.TITLE, "Repensando os conceitos no estudo de classificacão");
        inproceedings.setField(StandardField.BOOKTITLE, "Proceedings...");
        inproceedings.setField(StandardField.YEAR, "1982");
        inproceedings.setField(InternalField.KEY_FIELD, "chave-teste");
        inproceedings.setField(new UnknownField("conference-year"), "1982");
        inproceedings.setField(new UnknownField("conference-location"), "Augsburg. Universal Classification: Subject analysis and ordering systems");
        inproceedings.setField(new UnknownField("conference-number"), "4");
        return inproceedings;
    }






}
