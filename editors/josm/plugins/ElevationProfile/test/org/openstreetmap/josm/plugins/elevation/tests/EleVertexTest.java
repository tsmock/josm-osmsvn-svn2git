package org.openstreetmap.josm.plugins.elevation.tests;

import java.util.List;

import junit.framework.TestCase;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.elevation.EleCoordinate;
import org.openstreetmap.josm.plugins.elevation.EleVertex;
import org.openstreetmap.josm.plugins.elevation.ElevationRenderer;

public class EleVertexTest extends TestCase {

    /**
     * Setup test.
     */
    public void setUp() {
        Main.pref = new Preferences();
    }
    
    public void testDivide() {
    EleCoordinate p1 = new EleCoordinate(30.0, 30.0, 100.0);
    EleCoordinate p2 = new EleCoordinate(35.0, 30.0, 120.0);
    EleCoordinate p3 = new EleCoordinate(35.0, 40.0, 110.0);
    EleVertex ev = new EleVertex(p1, p2, p3);
    
    List<EleVertex> list = ev.divide();
    
    assertEquals(2, list.size());
    
    // 1st vertex (p1, p2, pN  105m) 
    EleVertex v1 = list.get(0);
    assertEquals(325 / 3D, v1.getEle());
    assertCoorEq(v1, 30D, 30D, 0);
    assertCoorEq(v1, 30D, 35D, 1);
    assertCoorEq(v1, 35D, 32.5D, 2);
    
    // 2nd vertex (p3, p2, pN = 105m) 
    EleVertex v2 = list.get(1);
    
    assertEquals(335/3D, v2.getEle());
    assertCoorEq(v2, 40D, 35D, 0);
    assertCoorEq(v2, 30D, 35D, 1);
    assertCoorEq(v2, 35D, 32.5D, 2);
    }
    
    public void testSimpleRecurse() {
    EleCoordinate c1 = new EleCoordinate(new LatLon(50.8328, 8.1337), 300);
    EleCoordinate c2 = new EleCoordinate(new LatLon(50.8328, 7.9217), 200);
    EleCoordinate c3 = new EleCoordinate(new LatLon(50.9558, 7.9217), 400);
    EleCoordinate c4 = new EleCoordinate(new LatLon(50.5767627, 9.1938483), 100);
    
    EleVertex v1 = new EleVertex(c1, c2, c3);
    System.out.println("Start recurse");
    recurse(v1, 0);
    }
    
    private void recurse(EleVertex v, int depth) {
    if (!v.isFinished() && depth <100) {
        System.out.println("\tDivide: " + v);
        List<EleVertex> list = v.divide();
        assertNotNull(list);
        assertEquals(2, list.size());
        assertTrue(depth < 50); //, "Too many recursions?");	    
        for (EleVertex eleVertex : list) {
        //System.out.println("\t\tRecurse: " + eleVertex);
        assertTrue("Area is larger " + v.getArea() + " > " + eleVertex.getArea(), eleVertex.getArea() < v.getArea());
        recurse(eleVertex, depth + 1);
        }
    } else {
        System.out.println("Finished: " + depth);
    }
    }
    
    public void testRenderer() {
    
    // Staufenberg, Hessen
    // Ulrichstein, Hessen
    ElevationRenderer er = new ElevationRenderer("Ele", new Bounds(
        new LatLon(50.6607106, 8.7337029), 
        new LatLon(50.5767627, 9.1938483)), null);
    
    er.run();
    }

    private void assertCoorEq(EleVertex v1, double x, double y, int n) {
    assertEquals(x, v1.get(n).getX());
    assertEquals(y, v1.get(n).getY());
    }
}
