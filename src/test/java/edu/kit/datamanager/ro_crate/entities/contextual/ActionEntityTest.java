package edu.kit.datamanager.ro_crate.entities.contextual;

import edu.kit.datamanager.ro_crate.HelpFunctions;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import org.junit.jupiter.api.Test;

/**
 *
 * @author sabrinechelbi
 */
class ActionEntityTest {

    @Test
    void testAddCreateActionMinimalExample() throws IOException {
        
        ContextualEntity equipment = new ContextualEntity.ContextualEntityBuilder()
                .setId("https://confluence.csiro.au/display/ASL/Hovermap")
                .addType("IndividualProduct")
                .addProperty("description", "The CSIRO bentwing is an unmanned aerial vehicle (UAV, commonly known as a drone) with a LIDAR ... ")
                .addProperty("name", "Bentwing")
                .addIdProperty("manufacturer", "https://www.atlassian.com/software/confluence")
                .addProperty("serialNumber", "1111122233321231")
                .build();
        
        assertNotNull(equipment);
        HelpFunctions.compareEntityWithFile(equipment, "/json/entities/contextual/equipment.json");

        ActionEntity createAction = new ActionEntity.ActionEntityBuilder(ActionTypeEnum.CREATE)
                .setId("#DataCapture_wcc02")
                .setAgent("https://orcid.org/0000-0002-1672-552X")
                .addInstrument("https://confluence.csiro.au/display/ASL/Hovermap")
                .addObject("#victoria_arch")
                // note how we can add multiple items by simply repeating them.
                .addResult("wcc02_arch.laz")
                .addResult("wcc02_arch_traj.txt")
                .addDateTimeProperty("startTime","2017-06-10T12:56:14+10:00")
                .addDateTimeProperty("endTime","2017-06-11T12:56:14+10:00")
                .build();
        
        assertNotNull(createAction);
        HelpFunctions.compareEntityWithFile(createAction, "/json/entities/contextual/createActionExample.json");
    }
    
    @Test
    void testAddCreateActionUsingSoftware() throws IOException {
        ContextualEntity software = new ContextualEntity.ContextualEntityBuilder()
                .setId("https://www.imagemagick.org/")
                .addType("SoftwareApplication")
                .addProperty("url", "https://www.imagemagick.org/")
                .addProperty("name", "ImageMagick")
                .addProperty("version", "ImageMagick 6.9.7-4 Q16 x86_64 20170114 http://www.imagemagick.org")
                .build();
        assertNotNull(software);
        HelpFunctions.compareEntityWithFile(software, "/json/entities/contextual/software.json");

        ActionEntity createAction = new ActionEntity.ActionEntityBuilder(ActionTypeEnum.CREATE)
                .setId("#Photo_Capture_1")
                .setAgent("https://orcid.org/0000-0002-3545-944X")
                .setDescription("Photo snapped on a photo walk on a misty day")
                .setEndTime("2017-06-11T12:56:14+10:00")
                .addInstrument("#EPL1")
                .addInstrument("#Panny20mm")
                .addResult("pics/2017-06-11%2012.56.14.jpg")
                .build();
        assertNotNull(createAction);
        HelpFunctions.compareEntityWithFile(createAction, "/json/entities/contextual/createActionExampleSoftware.json");
    }
    
    @Test
    void testAddUpdateAction() throws IOException {
        ActionEntity createAction = new ActionEntity.ActionEntityBuilder(ActionTypeEnum.UPDATE)
                .setId("#history-02")
                .setName("RO-Crate published")
                .addObject("https://doi.org/10.5281/zenodo.1009240")
                .setAgent("https://orcid.org/0000-0001-5152-5307")
                .setEndTime("2018-09-10")
                .addInstrument("https://stash.research.uts.edu.au")
                .addIdProperty("actionStatus", "http://schema.org/CompletedActionStatus")
                .build();
        assertNotNull(createAction);
        HelpFunctions.compareEntityWithFile(createAction, "/json/entities/contextual/updateAction.json");
    }
}
