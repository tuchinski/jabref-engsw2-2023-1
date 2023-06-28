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

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

@GUITest
@ExtendWith(ApplicationExtension.class)
public class SourceTabGenerateArticleTest {

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
    void testCreateArticleEmpty(){
        BibEntry article = new BibEntry(StandardEntryType.Article);

        database.insertEntry(article);

        sourceTab.createArticleCrossref(article);

        // verify if exists only one entry(the empty article entry)
        assertEquals(database.getEntryCount(), 1);
    }

    @Test
    void testCreateArticle(){
        BibEntry article = createArticleWithUtilFields();

        database.insertEntry(article);

        sourceTab.createArticleCrossref(article);

        String keyCreatedArticle = "misc-"  + article.getField(StandardField.PUBLISHER).get() + article.getField(StandardField.JOURNAL).get();
        keyCreatedArticle = keyCreatedArticle.replace(" ", "_");

        BibEntry expected = createExpectedMiscWithUtilFields(keyCreatedArticle);

        if(database.getEntryByCitationKey(keyCreatedArticle).isPresent()){
            BibEntry createdEntry = database.getEntryByCitationKey(keyCreatedArticle).get();
            assertEquals(expected, createdEntry);
        }else{
            fail("Entrada não foi criada com sucesso");
        }
    }

    @Test
    void testTryCreateArticleWithDuplicateValue(){
        BibEntry article = createArticleWithUtilFields();
        database.insertEntry(article);

        String keyCreatedArticle = "misc-"  + article.getField(StandardField.PUBLISHER).get() + article.getField(StandardField.JOURNAL).get();
        keyCreatedArticle = keyCreatedArticle.replace(" ", "_");

        BibEntry miscCreated = createExpectedMiscWithUtilFields(keyCreatedArticle);
        database.insertEntry(miscCreated);

        sourceTab.createArticleCrossref(article);

        // check if exists only 2 entries in database(article and misc that we already add to database)
        assertEquals(database.getEntryCount(), 2);
    }
    @Test
    void testCreateArticleWithoutUtilFields(){
        BibEntry article = createArticleWithUtilFields();
        article.clearField(StandardField.PUBLISHER);
        article.clearField(StandardField.JOURNAL);

        database.insertEntry(article);

        sourceTab.createArticleCrossref(article);

        // verify if exists only one entry(the article entry without util fields to extract)
        assertEquals(database.getEntryCount(), 1);
    }

    private static BibEntry createExpectedMiscWithUtilFields(String keyCreatedArticle) {
        BibEntry expected = new BibEntry(StandardEntryType.Misc);
        expected.setField(StandardField.JOURNAL, "CoRR");
        expected.setField(StandardField.PUBLISHER, "publisher teste");
        expected.setField(InternalField.KEY_FIELD, keyCreatedArticle);
        return expected;
    }

    private static BibEntry createArticleWithUtilFields() {
        BibEntry article = new BibEntry(StandardEntryType.Article);
        article.setField(StandardField.AUTHOR, "Negar Hashemi and Amjed Tahir and Shawn Rasheed");
        article.setField(StandardField.JOURNAL, "CoRR");
        article.setField(StandardField.TITLE, "An Empirical Study of Flaky Tests in JavaScript");
        article.setField(StandardField.YEAR, "2022");
        article.setField(StandardField.VOLUME, "abs/2207.01047");
        article.setField(StandardField.ARCHIVEPREFIX, "arXiv");
        article.setField(new UnknownField("bibsource"), "dblp computer science bibliography, https://dblp.org");
        article.setField(new UnknownField("biburl"), "https://dblp.org/rec/journals/corr/abs-2207-01047.bib");
        article.setField(StandardField.DOI, "10.48550/arXiv.2207.01047");
        article.setField(StandardField.EPRINT, "2207.01047");
        article.setField(InternalField.KEY_FIELD, "Hashemi2022a");
        article.setField(StandardField.PUBLISHER, "publisher teste");
        return article;
    }
}
