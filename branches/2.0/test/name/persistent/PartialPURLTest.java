package name.persistent;

import junit.framework.TestCase;

import name.persistent.concepts.PURL;
import name.persistent.concepts.PartialPURL;
import name.persistent.concepts.Resolvable;
import name.persistent.concepts.ZonedPURL;

import org.apache.http.HttpResponse;
import org.openrdf.http.object.exceptions.NotFound;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.object.config.ObjectRepositoryFactory;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;


public class PartialPURLTest extends TestCase {
	private static final String NS = "http://persistent.name/rdf/2010/purl#";
	private static final String PURL0 = "http://test.persistent.name/test/test0/";
	private static final String PURL1 = "http://test.persistent.name/test/test1/";
	private ObjectRepository repo;
	private ObjectConnection con;

	public void setUp() throws Exception {
		ObjectRepositoryFactory orf = new ObjectRepositoryFactory();
		SailRepository sail = new SailRepository(new MemoryStore());
		sail.initialize();
		repo = orf.createRepository(sail);
		con = repo.getConnection();
		ValueFactory vf = con.getValueFactory();
		URI rel = vf.createURI(NS, "rel");
		con.add(vf.createURI(NS, "alternative"), rel, vf.createLiteral("alternative"));
	}

	public void tearDown() throws Exception {
		con.close();
		repo.shutDown();
	}

	public void testRegex() throws Exception {
		PartialPURL purl = con.addDesignation(con.getObject(PURL0),
				PartialPURL.class);
		purl.getPurlAlternatives().add(con.getObject("http://docs.$1/pages/$2.html"));
		purl.setPurlPattern("http://test.([^/]*)/(.*)");
		HttpResponse resp = resolvePURL("http://test.persistent.name/test/test0/item");
		assertEquals(302, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals("http://docs.persistent.name/pages/test/test0/item.html",
				resp.getFirstHeader("Location").getValue());
	}

	public void testPathFragment() throws Exception {
		PURL purl = con.addDesignation(con.getObject(PURL0), PartialPURL.class);
		purl.getPurlAlternatives().add(con.getObject(PURL1));
		HttpResponse resp = resolvePURL("http://test.persistent.name/test/test0/item");
		assertEquals(302, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals(PURL1, resp.getFirstHeader("Location").getValue());
	}

	public void testZonedPURL() throws Exception {
		PURL purl = con.addDesignation(con.getObject(PURL0), ZonedPURL.class);
		purl.getPurlAlternatives().add(con.getObject(PURL1));
		HttpResponse resp = resolvePURL("http://my.test.persistent.name/test/test0/");
		assertEquals(302, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals(PURL1, resp.getFirstHeader("Location").getValue());
	}

	public void testZonedPartialPURL() throws Exception {
		PURL purl = con.addDesignation(con.getObject(PURL0), ZonedPURL.class);
		purl = con.addDesignation(purl, PartialPURL.class);
		purl.getPurlAlternatives().add(con.getObject(PURL1));
		HttpResponse resp = resolvePURL("http://my.test.persistent.name/test/test0/item");
		assertEquals(302, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals(PURL1, resp.getFirstHeader("Location").getValue());
	}

	public void testZonedPartialPatternPURL() throws Exception {
		PartialPURL purl = con.addDesignation(con.getObject(PURL0), PartialPURL.class);
		purl = (PartialPURL) con.addDesignation(purl, ZonedPURL.class);
		purl.getPurlAlternatives().add(con.getObject("http://docs.$2/pages/$1/$3.html"));
		purl.setPurlPattern("http://([^.]*)\\.test\\.([^/]*)/.*/([^/]*)");
		HttpResponse resp = resolvePURL("http://my.test.persistent.name/test/test0/item");
		assertEquals(302, resp.getStatusLine().getStatusCode());
		assertEquals(1, resp.getHeaders("Location").length);
		assertEquals("http://docs.persistent.name/pages/my/item.html", resp.getFirstHeader("Location").getValue());
	}

	public void testUnknownPURL() throws Exception {
		PURL purl = con.addDesignation(con.getObject(PURL0), PURL.class);
		purl.getPurlAlternatives().add(con.getObject(PURL1));
		try {
			resolvePURL("http://my.test.persistent.name/test/test0/");
			fail();
		} catch (NotFound e) {
		}
	}

	private HttpResponse resolvePURL(String uri) throws Exception {
		Resolvable target = (Resolvable) con.getObject(uri);
		return target.resolvePURL(uri, null, null, "*", 20);
	}
}