<%--
  Created by IntelliJ IDEA.
  User: jrobins
  Date: 1/12/15
  Time: 5:37 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.solidify.dao.SignatureOld" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="com.aspose.pdf.drawing.Graph" %>
<%@ page import="com.aspose.pdf.drawing.Line" %>
<%@ page import="java.awt.image.BufferedImage" %>
<%@ page import="signaturepad.SignatureToImage" %>
<%@ page import="java.io.File" %>
<%@ page import="javax.imageio.ImageIO" %>
<%@ page import="aspose.pdf.*" %>
<%@ page import="aspose.pdf.Row" %>
<%@ page import="aspose.pdf.Cell" %>
<%@ page import="aspose.pdf.BorderInfo" %>
<%@ page import="aspose.pdf.BorderSide" %>
<%@ page import="aspose.pdf.PageSize" %>
<%@ page import="aspose.pdf.Table" %>
<%@ page import="aspose.pdf.Image" %>


<%
  /*Signature sig = new Signature("e684888c-19d2-433b-b8ed-b02fe7529ac3");
  JSONObject data = sig.getDataPoints();
  JSONArray ja = data.getJSONArray("signature");
  BufferedImage img = SignatureToImage.convertJsonToImage(ja.toString());
  File outputfile = new File("/tmp/saved.png");
  ImageIO.write(img, "png", outputfile);*/

  Pdf pdf = new Pdf();
  pdf.getPageSetup().setPageHeight(PageSize.LETTER_HEIGHT);
  pdf.getPageSetup().setPageWidth(PageSize.LETTER_WIDTH);
  MarginInfo mi = new MarginInfo();
  mi.setLeft(10f);
  mi.setRight(10f);
  mi.setTop(100f);
  mi.setBottom(100f);
  pdf.getPageSetup().setMargin(mi);
  Section sec = pdf.getSections().add();

  Table table1 = new Table();
  table1.setColumnWidths("4inch 4inch");
  table1.setDefaultCellBorder(new BorderInfo((int) BorderSide.All, 0.5F));

  sec.getParagraphs().add(table1);
  Image png = new Image();
  png.getImageInfo().setImageFileType(ImageFileType.Png);
  png.getImageInfo().setFile("/tmp/saved.png");
  /*sec.getParagraphs().add(png);
  Text text = new Text(sec,"image Here");
  text.getTextInfo().setFontName("helvetica");
  sec.getParagraphs().add(text);*/

  Row row1 = table1.getRows().add();
  Cell cell1Row1 = row1.getCells().add();
  cell1Row1.getParagraphs().add(png);

  Cell cell2Row1 = row1.getCells().add();
  Text text = new Text(sec,"Jon Robins");
  text.getTextInfo().setFontName("helvetica");
  cell2Row1.getParagraphs().add(text);

  pdf.save("/tmp/sigs.pdf");

  /*int len = ja.length();
  float[] points = new float[len*4];
  int x = 0;
  for (int i=0; i<len; i++) {
    JSONObject jo = ja.getJSONObject(i);
    points[x] = jo.getInt("lx");
    x++;
    points[x] = jo.getInt("ly");
    x++;
    points[x] = jo.getInt("mx");
    x++;
    points[x] = jo.getInt("my");
    x++;
  }
  Graph graph = new Graph(400,100);
  Document doc = new Document();

  Page p = doc.getPages().add();



  p.getParagraphs().add(graph);

  Line line = new Line(points);

  graph.getShapes().add(line);

  doc.save("/tmp/testGraph.pdf");*/
%>
<html>
<head>
    <title></title>
</head>
<body>
Done
</body>
</html>
