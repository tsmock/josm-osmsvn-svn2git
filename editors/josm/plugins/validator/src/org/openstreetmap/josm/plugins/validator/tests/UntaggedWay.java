package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Map;

import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.validator.*;
/**
 * Checks for untagged ways
 * 
 * @author frsantos
 */
public class UntaggedWay extends Test 
{
    /** Tags allowed in a way */
    public static String[] allowedTags = new String[] { "created_by", "converted_by" };
    
    /**
     * Constructor
     */
    public UntaggedWay() 
    {
        super(tr("Untagged ways."),
              tr("This test checks for untagged ways."));
    }

    @Override
    public void visit(Way w) 
    {
        int numTags = 0;
        Map<String, String> tags = w.keys;
        if( tags != null )
        {
            numTags = tags.size();
            for( String tag : allowedTags)
                if( tags.containsKey(tag) ) numTags--;
            
            if( numTags != 0 && tags.containsKey("highway" ) )
            {
                if( !tags.containsKey("name") && !tags.containsKey("ref") )
                {
                    boolean hasName = false;
                    for( String key : w.keySet())
                    {
                        hasName = key.startsWith("name:") || key.endsWith("_name") || key.endsWith("_ref");
                        if( hasName )
                            break;
                    }
                    
                    if( !hasName)
                        errors.add( new TestError(this, Severity.WARNING, tr("Unnamed ways"), w) );
                }
            }
        }
        
        if( numTags == 0 )
        {
            errors.add( new TestError(this, Severity.WARNING, tr("Untagged ways"), w) );
        }
    }		
}
