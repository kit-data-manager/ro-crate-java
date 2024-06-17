package edu.kit.datamanager.ro_crate.entities.contextual;

import edu.kit.datamanager.ro_crate.HelpFunctions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 *
 * @author sabrinechelbi
 */
public class ActionEntityTest {

    @Test
    void testAddCreateActionMinimalExample() throws IOException {
        
        String idEquipment = "https://confluence.csiro.au/display/ASL/Hovermap";
        ContextualEntity equipment = new ContextualEntity.ContextualEntityBuilder()
                .setId(idEquipment)
                .addType("IndividualProduct")
                .addProperty("description", "The CSIRO bentwing is an unmanned aerial vehicle (UAV, commonly known as a drone) with a LIDAR ... ")
                .addProperty("name", "Bentwing")
                .addIdProperty("manufacturer", "https://www.atlassian.com/software/confluence")
                .addProperty("serialNumber", "1111122233321231")
                .build();
        HelpFunctions.compareEntityWithFile(equipment, "/json/entities/contextual/equipment.json");
        

        String idAction = "#DataCapture_wcc02";
        List<String> instrument = new ArrayList<>();
        instrument.add("https://confluence.csiro.au/display/ASL/Hovermap");
        List<String> object = new ArrayList<>();
        object.add("#victoria_arch");
        List<String> result = new ArrayList<>();
        result.add("wcc02_arch.laz");
        result.add("wcc02_arch_traj.txt");
        ActionEntity createAction = new ActionEntity.ActionEntityBuilder()
                .setId(idAction)
                .addType(ActionTypeEnum.CREATE)
                .addAgent("https://orcid.org/0000-0002-1672-552X")
                .addInstrument(instrument)
                .addObject(object)
                .addResult(result)
                .addDateTimeProperty("startTime","2017-06-10T12:56:14+10:00")
                .addDateTimeProperty("endTime","2017-06-11T12:56:14+10:00")
                .build();
                
        HelpFunctions.compareEntityWithFile(createAction, "/json/entities/contextual/createActionExample.json");
    }
    
    @Test
    void testAddCreateActionUsingSoftware() throws IOException {
        
         String idSoftware = "https://www.imagemagick.org/";
        ContextualEntity software = new ContextualEntity.ContextualEntityBuilder()
                .setId(idSoftware)
                .addType("SoftwareApplication")
                .addProperty("url", "https://www.imagemagick.org/")
                .addProperty("name", "ImageMagick")
                .addProperty("version", "ImageMagick 6.9.7-4 Q16 x86_64 20170114 http://www.imagemagick.org")
                .build();
        HelpFunctions.compareEntityWithFile(software, "/json/entities/contextual/software.json");

        String id = "#Photo_Capture_1";
        List<String> instrument = new ArrayList<>();
        instrument.add("#EPL1");
        instrument.add("#Panny20mm");
        List<String> result = new ArrayList<>();
        result.add("pics/2017-06-11%2012.56.14.jpg");
        ActionEntity createAction = new ActionEntity.ActionEntityBuilder()
                .setId(id)
                .addType(ActionTypeEnum.CREATE)
                .addAgent("https://orcid.org/0000-0002-3545-944X")
                .addDescription("Photo snapped on a photo walk on a misty day")
                .addEndTime("2017-06-11T12:56:14+10:00")
                .addInstrument(instrument)
                .addResult(result)
                .build();
        HelpFunctions.compareEntityWithFile(createAction, "/json/entities/contextual/createActionExampleSoftware.json");
    }
    
    @Test
    void testAddUpdateAction() throws IOException {

        String id = "#history-02";
        List<String> instrument = new ArrayList<>();
        instrument.add("https://stash.research.uts.edu.au");
         List<String> object = new ArrayList<>();
        object.add("https://doi.org/10.5281/zenodo.1009240");
        ActionEntity createAction = new ActionEntity.ActionEntityBuilder()
                .setId(id)
                .addType(ActionTypeEnum.UPDATE)
                .addObject(object)
                .addName("RO-Crate published")
                .addAgent("https://orcid.org/0000-0001-5152-5307")
                .addEndTime("2018-09-10")
                .addInstrument(instrument)
                .addIdProperty("actionStatus", "http://schema.org/CompletedActionStatus")
                .build();
        HelpFunctions.compareEntityWithFile(createAction, "/json/entities/contextual/updateAction.json");
    }
}
