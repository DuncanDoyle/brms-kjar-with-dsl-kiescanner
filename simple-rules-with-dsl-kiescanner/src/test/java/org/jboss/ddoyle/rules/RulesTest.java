package org.jboss.ddoyle.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.kie.scanner.MavenRepository.getMavenRepository;

import java.io.File;
import java.io.IOException;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.core.util.FileManager;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.ReleaseId;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.scanner.AbstractKieCiTest;
import org.kie.scanner.MavenRepository;

/**
 * Tests reloading of a KJAR with an RDSLR. 
 * 
 * @author <a href="mailto:ddoyle@redhat.com">Duncan Doyle</a>
 */
public class RulesTest extends AbstractKieCiTest {


	private FileManager fileManager;
	private File kPom;
	private ReleaseId releaseId;

	@Before
	public void setUp() throws Exception {
		this.fileManager = new FileManager();
		this.fileManager.setUp();
		releaseId = KieServices.Factory.get().newReleaseId("org.kie", "scanner-test", "1.0-SNAPSHOT");
		kPom = createKPom(releaseId);
	}

	@Test
	public void checkIncrementalCompilation() throws IOException {
		KieServices ks = KieServices.Factory.get();
		MavenRepository repository = getMavenRepository();

		InternalKieModule kJar1 = createFirstKieJar(ks, releaseId);
		repository.deployArtifact(releaseId, kJar1, kPom);

		KieContainer kieContainer = ks.newKieContainer(releaseId);
		
		KieScanner scanner = ks.newKieScanner(kieContainer);
		
		runTest(kieContainer, new SimpleFact("1"), 1);
		
		InternalKieModule kJar2 = createSecondKieJar(ks, releaseId);
		repository.deployArtifact(releaseId, kJar2, kPom);

		scanner.scanNow();
		
		runTest(kieContainer, new SimpleFact("1"), 0);
		runTest(kieContainer, new SimpleFact("2"), 1);

		ks.getRepository().removeKieModule(releaseId);
	}

	private void runTest(KieContainer kieContainer, SimpleFact simpleFact, long expectedRulesFired) {
    	KieSession kieSession = kieContainer.newKieSession();
		RulesFiredListener rulesFiredListener = new RulesFiredListener();
		kieSession.addEventListener(rulesFiredListener);

		kieSession.insert(simpleFact);
		kieSession.fireAllRules();

		assertEquals("One rule should have fired.", expectedRulesFired, rulesFiredListener.getNrOfRulesFired());
    }

	/**
	 * Creates the first KJAR.
	 * 
	 * @param ks
	 * @param releaseId
	 * @return
	 */
	private InternalKieModule createFirstKieJar(KieServices ks, ReleaseId releaseId) {
		KieFileSystem kfs = createKieFileSystemWithKProject(ks, true);
		kfs.writePomXML(getPom(releaseId));
		
		Resource dslResource = ks.getResources().newClassPathResource("org/jboss/ddoyle/rules/mycustom.dsl");
		kfs.write("src/main/resources/KBase1/mycustom.dsl", dslResource);
		
		Resource rdslrResource = ks.getResources().newClassPathResource("org/jboss/ddoyle/rules/myrules.rdslr");
		kfs.write("src/main/resources/KBase1/myrules.rdslr", rdslrResource);

		KieBuilder kieBuilder = ks.newKieBuilder(kfs);
		assertTrue("", kieBuilder.buildAll().getResults().getMessages().isEmpty());
		return (InternalKieModule) kieBuilder.getKieModule();
	}

	/**
	 * Creates the second KJAR.
	 * 
	 * @param ks
	 * @param releaseId
	 * @return
	 */
	private InternalKieModule createSecondKieJar(KieServices ks, ReleaseId releaseId) {
		KieFileSystem kfs = createKieFileSystemWithKProject(ks, true);
		kfs.writePomXML(getPom(releaseId));
		
		Resource dslResource = ks.getResources().newClassPathResource("org/jboss/ddoyle/rules/mycustom.dsl");
		kfs.write("src/main/resources/KBase1/mycustom.dsl", dslResource);
		
		Resource rdslrResource = ks.getResources().newClassPathResource("org/jboss/ddoyle/rules/myrules-2.rdslr");
		kfs.write("src/main/resources/KBase1/myrules.rdslr", rdslrResource);

		KieBuilder kieBuilder = ks.newKieBuilder(kfs);
		assertTrue("", kieBuilder.buildAll().getResults().getMessages().isEmpty());
		return (InternalKieModule) kieBuilder.getKieModule();

	}

	private File createKPom(ReleaseId releaseId) throws IOException {
		File pomFile = fileManager.newFile("pom.xml");
		fileManager.write(pomFile, getPom(releaseId));
		return pomFile;
	}
	
}
