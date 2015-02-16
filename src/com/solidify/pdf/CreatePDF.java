package com.solidify.pdf;

//import com.aspose.pdf.*;
//import com.aspose.pdf.drawing.Graph;
//import com.aspose.pdf.drawing.Line;
import com.solidify.dao.*;
import com.solidify.dao.Signature;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by jrobins on 1/12/15.
 */
public class CreatePDF {
    public static void main(String[] args) throws Exception
    {
        String dataDir = "/tmp/";

       /* Document doc = new Document();
        Page page = (Page)doc.getPages().add();
        TextFragment frag = new TextFragment("My text goes here");
        frag.setPosition(new Position(10,800));
        TextBuilder tb = new TextBuilder(page);
        tb.appendText(frag);
        doc.save(dataDir+"test.pdf");*/

       // Signature sig = new Signature("083b9cbb-9af6-46c9-b542-72b9d9d4b902");
        //JSONObject data = sig.getDataPoints();
        //System.out.println(data.toString());

        /*Document doc = new Document();
        Page page = doc.getPages().add();
        Graph graph = new Graph(400,100);
        page.getParagraphs().add(graph);
        Line line = new Line(new float[]{41,38,41,37,60,37,41,38,92,37,60,37,121,37,92,37,139,37,121,37,154,37,139,37,162,37,154,37,168,38,162,37,172,38,168,38,179,39,172,38,184,39,179,39,185,40,184,39,188,40,185,40,188,41,188,40,189,41,188,41});
        graph.getShapes().add(line);
        doc.save("/tmp/testGraph.pdf");*/
    }
}

