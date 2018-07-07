import com.itextpdf.text.PageSize;
import org.gephi.appearance.RankingImpl;
import org.gephi.appearance.api.Ranking;
import org.gephi.appearance.plugin.RankingLabelColorTransformer;
import org.gephi.graph.api.*;
import org.gephi.graph.api.types.TimeSet;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.preview.PDFExporter;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.generator.plugin.RandomGraph;
import org.gephi.io.importer.api.*;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.impl.EdgeDraftImpl;
import org.gephi.io.importer.impl.NodeDraftImpl;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;


public class GraphBuilder {
    private static GraphBuilder shared = new GraphBuilder();
    private static JFrame frame = new JFrame("GraphBuilder");
    private JTextField dirField;
    private JButton openButton;
    private JButton buildButton;
    private JProgressBar buildProgress;
    private JTextField nodeField;
    private JTextField edgeField;
    private JPanel panel1;
    private DefaultBoundedRangeModel dm = new DefaultBoundedRangeModel();
    private File f;
    public GraphBuilder() {
        buildProgress.setStringPainted(true);
        openButton.addActionListener(e->{

            JFileChooser fch = new JFileChooser();
            fch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fch.showOpenDialog(frame);
            if(result == JFileChooser.APPROVE_OPTION){
                f = fch.getSelectedFile();
                dirField.setText(f.getAbsolutePath());
            }
        });
        buildButton.addActionListener(e ->{
            try {
                processFiles();
            }catch (Exception e1){
                e1.printStackTrace();
            }

        });
    }
    private void processFiles() throws Exception{
        SwingWorker<Void,Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                File[] dirs = f.listFiles();
                dm = new DefaultBoundedRangeModel(0,1,0,dirs.length);
                buildProgress.setModel(dm);
                for(int i = 0; i < dirs.length;i++){
                    publish(i);
                    createGraph(dirs[i]);
                }
                publish(dirs.length);
                JOptionPane.showMessageDialog(frame,"Successfuly built the graphs!","Success",JOptionPane.INFORMATION_MESSAGE);
                publish(0);
                return null;
            }
            @Override
            protected void process(java.util.List<Integer> chunks){
                int progress = chunks.get(chunks.size()-1);
                dm.setValue(progress);
                buildProgress.updateUI();
            }
        };
        worker.execute();
    }
    protected void createGraph(File dir) throws Exception{
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace wksp = pc.getCurrentWorkspace();
        Container container = Lookup.getDefault().lookup(org.gephi.io.importer.api.Container.Factory.class).newContainer();
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dir.getAbsolutePath()+"/nodes.csv"))));
        String line = "";
        HashMap<String,Node> nodes = new HashMap<String,Node>();
        while((line=br.readLine()) != null){
            if(line.contains("Id")){continue;}
            String[] parts = line.split(",");
            String id = parts[0].trim();
            String title = parts[1].trim();
            float weight = Float.valueOf(parts[2].trim());
            Node n = graphModel.factory().newNode(id);
            n.setLabel(title);
            n.setSize(weight);
            //System.out.println(Arrays.toString(n.getAttributes()));
            nodes.put(id,n);
        }
        br.close();
        br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dir.getAbsolutePath()+"/edges.csv"))));
        int i = 0;
        LinkedList<Edge> edges = new LinkedList<>();
        while((line=br.readLine()) != null){
            if(line.contains("Source")){continue;}
            String[] parts = line.split(",");
            String source = parts[0].trim();
            String target = parts[1].trim();
            double weight = Float.valueOf(parts[2].trim());
            Edge e = graphModel.factory().newEdge(nodes.get(source),nodes.get(target),true);
            e.setWeight(weight);
            edges.add(e);
            i++;
        }
        DirectedGraph graph = graphModel.getDirectedGraph();
        graph.addAllNodes(nodes.values());
        graph.addAllEdges(edges);
        RankingLabelColorTransformer ranker = new RankingLabelColorTransformer();
        PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();

        PreviewProperties prop = model.getProperties();
        prop.putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        prop.putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.GRAY));
        prop.putValue(PreviewProperty.EDGE_THICKNESS, new Float(0.1f));
        prop.putValue(PreviewProperty.NODE_LABEL_FONT, prop.getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(12));

        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        ec.exportFile(new File(dir.getAbsolutePath()+"/io_gexf.gexf"));
        //Export to Writer
        Exporter exporterGraphML = ec.getExporter("graphml");     //Get GraphML exporter
        exporterGraphML.setWorkspace(wksp);
        StringWriter stringWriter = new StringWriter();
        ec.exportWriter(stringWriter, (CharacterExporter) exporterGraphML);

        PDFExporter pdfExporter = (PDFExporter) ec.getExporter("pdf");
        pdfExporter.setPageSize(PageSize.LETTER);
        pdfExporter.setWorkspace(wksp);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ec.exportStream(baos, pdfExporter);
        byte[] pdf = baos.toByteArray();
        br.close();
        FileOutputStream fos = new FileOutputStream(dir.getAbsolutePath()+"/image.pdf");
        fos.write(pdf);
        fos.close();

    }




    public static void main(String[] args) {
        frame.setContentPane(shared.panel1);
        frame.setMinimumSize(new Dimension(480,360));
        frame.setSize(800,600);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}