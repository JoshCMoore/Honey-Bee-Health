import com.itextpdf.text.PageSize;
import org.gephi.appearance.api.*;
import org.gephi.appearance.plugin.PartitionElementColorTransformer;
import org.gephi.appearance.plugin.RankingElementColorTransformer;
import org.gephi.appearance.plugin.palette.Palette;
import org.gephi.appearance.plugin.palette.PaletteManager;
import org.gephi.graph.api.*;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.preview.PDFExporter;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;

/** Created by Andrew Strickland. GraphBuilder is a gui and cli tool for generating Gephi graphs
* from research data. 
* Update as of 06/09/2018, GraphBuilder doesnt successfully export the project file for some
* unknown reason. Until I can look more into it I assume it is a gephi toolkit issue. Graph layouts
* must also be applied manually in gephi, Graphbuilder creates the graph, sets node sizes, names, and colors, 
* creates edges, and sets edge weights.
*/
public class GraphBuilder {
    private static String nodeName = "nodes.csv";
    private static String edgesName = "edges.csv";
    private static GraphBuilder shared = new GraphBuilder();
    private static JFrame frame = new JFrame("GraphBuilder");
    private ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
    private Project project1;
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
        edgeField.setText(edgesName);
        nodeField.setText(nodeName);
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
                if(validateName(edgeField.getText())){
                    edgesName = edgeField.getText().trim();
                }
                if(validateName(nodeField.getText())){
                    nodeName = nodeField.getText().trim();
                }
                processFiles(()->{
                    JOptionPane.showMessageDialog(frame,"Successfuly built the graphs!","Success",JOptionPane.INFORMATION_MESSAGE);
                });
            }catch (Exception e1){
                e1.printStackTrace();
            }

        });
    }
    private static boolean validateName(String name){
        return name != null && name.length() > 0 && name.trim().endsWith(".csv");
    }
    private void processFiles(Runnable completion){
        SwingWorker<Void,Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground(){
                File[] dirs = f.listFiles();
                dm = new DefaultBoundedRangeModel(0,1,0,dirs.length);
                buildProgress.setModel(dm);
                pc.newProject();
                project1 = pc.getCurrentProject();
                System.out.println("Building graphs:");
                for(int i = 0; i < dirs.length;i++){
                    publish(i);
                    if(!dirs[i].isDirectory()){continue;}
                    try {
                        System.out.print("\nBuilding graph: "+i+"/"+(dirs.length-1)+"...");
                        createGraph(dirs[i]);
                    }catch (Exception e){
                        System.out.println("An error ocurred creating graph: "+i+"/"+(dirs.length-1));
                    }
                }
                publish(dirs.length);
                pc.saveProject(project1,new File(f.getAbsolutePath()+"/project1.gephi")).run();
                completion.run();
                System.out.println("Finished!");
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
    /**Scales edge weights so that larger wieghts have a smaller value indicator a closer connection
    */
    private double edgeWeightAdjustment(double w){
        double scale = 129;
        return scale*(1.0/w);
    }
    
    protected void createGraph(File dir) throws Exception{

        Workspace wksp = pc.newWorkspace(project1);
        pc.openWorkspace(wksp);
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
        AppearanceModel appearanceModel = appearanceController.getModel();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dir.getAbsolutePath()+"/"+nodeName))));
        String line = "";
        HashMap<String,Node> nodes = new HashMap<String,Node>();
        System.out.print("Adding Nodes...");
        while((line=br.readLine()) != null){
            if(line.contains("Id")){continue;}
            String[] parts = line.split(",");
            String id = parts[0].trim();
            String title = parts[1].trim();
            float weight = Float.valueOf(parts[2].trim());
            Node n = graphModel.factory().newNode(id);
            n.setLabel(title);
            n.setSize(2*weight);
            nodes.put(id,n);
        }
        System.out.print("Finished! Adding edges...");
        br.close();
        br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dir.getAbsolutePath()+"/"+edgesName))));
        int i = 0;
        LinkedList<Edge> edges = new LinkedList<>();
        while((line=br.readLine()) != null){
            if(line.contains("Source")){continue;}
            String[] parts = line.split(",");
            String source = parts[0].trim();
            String target = parts[1].trim();
            double weight = Float.valueOf(parts[2].trim());
            Edge e = graphModel.factory().newEdge(nodes.get(source),nodes.get(target),false);
            e.setWeight(edgeWeightAdjustment(weight));
            edges.add(e);
            i++;
        }
        br.close();
        System.out.print("Finished! Finalizing...");
        UndirectedGraph graph = graphModel.getUndirectedGraph();
        graph.addAllNodes(nodes.values());
        graph.addAllEdges(edges);
        PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
        PreviewModel previewModel = previewController.getModel();
        previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);

        Function degreeRanking = appearanceModel.getNodeFunction(graph, AppearanceModel.GraphFunction.NODE_DEGREE, RankingElementColorTransformer.class);
        RankingElementColorTransformer degreeTransformer = (RankingElementColorTransformer) degreeRanking.getTransformer();
        degreeTransformer.setColors(new Color[]{new Color(0xFEF0D9), new Color(0xB30000)});
        degreeTransformer.setColorPositions(new float[]{0f, 1f});
        appearanceController.transform(degreeRanking);
        pc.closeCurrentWorkspace();
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        ec.exportFile(new File(dir.getAbsolutePath()+"/io_gexf.gexf"));
        Exporter exporterGraphML = ec.getExporter("graphml");     //Get GraphML exporter
        exporterGraphML.setWorkspace(wksp);
        StringWriter stringWriter = new StringWriter();
        ec.exportWriter(stringWriter, (CharacterExporter) exporterGraphML);
        System.out.println("Done!");
        pc.closeCurrentWorkspace();
//        PDFExporter pdfExporter = (PDFExporter) ec.getExporter("pdf");
//        pdfExporter.setPageSize(PageSize.LETTER);
//        pdfExporter.setWorkspace(wksp);
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        ec.exportStream(baos, pdfExporter);
//        byte[] pdf = baos.toByteArray();
//        FileOutputStream fos = new FileOutputStream(dir.getAbsolutePath()+"/image.pdf");
//        fos.write(pdf);
//        fos.close();

    }
    /**
    * Entry point.
    */
    public static void main(String[] args) {
        if(args.length > 0){
            // This is a command line, headless job
            try {
                String path = args[0];
                if(path.equals("--help")){
                    System.out.println("GraphBuilder constructs a gephi graph for each input year.");
                    System.out.print("\tEnsure your file structure is as follows:");
                    System.out.println("PATH_DIR/YEAR1/{nodes and edges files}...ex: GephiFiles/1957");
                    System.out.println("\t-Arg1(REQUIRED): PATH_DIR Parent folder containing yearly subfolders");
                    System.out.println("\t-Arg2(OPTIONAL): NODE_FILE_NAME Naming convention of the nodes file");
                    System.out.println("\t-Arg3(OPTIONAL): EDGE_FILE_NAME Naming convention of the edges file");
                    System.exit(0);
                }
                if(1 < args.length && validateName(args[1])){
                    nodeName = args[1].trim();
                }
                if(2 < args.length && validateName(args[2])){
                    edgesName = args[2].trim();
                }
                shared.f = new File(path);
                shared.processFiles(()->{
                    // when we are finished, close the program
                    System.exit(0);
                });
            }catch (IndexOutOfBoundsException e){
                System.out.println("Arguments should be as follows: REQUIRED path to directory of yearly data, OPTIONAL name of nodes file, OPTIONAL name of edges file");
            }
        }else {
            setupWindow();
        }
    }
    private static void setupWindow(){
        frame.setContentPane(shared.panel1);
        frame.setMinimumSize(new Dimension(480, 360));
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
