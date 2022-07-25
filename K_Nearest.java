import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.io.Opener;
import ij.io.SaveDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

import java.lang.Math;
import java.io.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class K_Nearest implements PlugInFilter{
    ImagePlus reference;        // Reference image
    int k;                      // Number of nearest neighbors
    int degree;                 // Zernike degree
    String dirFeatureVectors;   // Path to output dirs

    public int setup(String arg, ImagePlus imp) {
        reference = imp;
        ImageConverter ic = new ImageConverter(imp);
        ic.convertToGray8();
        return DOES_ALL;
    }

    public void run(ImageProcessor img) {


        GenericDialog gd = new GenericDialog("Zernike Moments", IJ.getInstance());
//        gd.addNumericField("Number of nearest neighbors (K):", 1, 0);
        gd.addNumericField("Degree:", 1, 0);
        gd.showDialog();
        if (gd.wasCanceled())
            return;
//        k = (int) gd.getNextNumber();
        degree = (int) gd.getNextNumber();

        SaveDialog sd = new SaveDialog("Open search folder...", "any file (required)", "");
        if (sd.getFileName() == null) return;
        String dir = sd.getDirectory();

        search(dir);
    }

    public void search(String dir) {
        IJ.log("");
        IJ.log("Searching images");
        if (!dir.endsWith(File.separator))
            dir += File.separator;
        String[] list = new File(dir).list();  /* lista de arquivos */
        if (list == null) return;

        // Caminho para o novo diretório onde serão salvos os vetores de característica gerados
        this.dirFeatureVectors = dir + "feature-vectors";

        for (int i = 0; i < list.length; i++) {
            IJ.showStatus(i + "/" + list.length + ": " + list[i]);   /* mostra na interface */
            IJ.showProgress((double) i / list.length);  /* barra de progresso */
            File f = new File(dir + list[i]);
            if (!f.isDirectory()) {
                ImagePlus image = new Opener().openImage(dir, list[i]); /* abre imagem image */

                if (image != null) {
                    // CODIGO

                    // Escrever o momento para a imagem de referencia
                    if (i == 0) {
                        IJ.log("");
                        IJ.log("Calculating and writing moments in .TXT File...");
                        Zernike img = new Zernike(reference);
                        img.zernikeMoments(degree);
                        // Teste p/ caso existir o diretório apagá-lo (caso rodar mais de uma vez)
                        File dirF = new File(dirFeatureVectors);
                        if(dirF.exists()) {
                            deleteDir(dirF);
                            dirF.delete();
                        }

                        // Escrevendo os momentos no arquivo "moments.txt"
                        img.writeMoments(dirFeatureVectors, "moments.txt", "imagespath.txt");
                    }
                        //image.show();

                    Zernike img = new Zernike(image);
                    img.zernikeMoments(degree);
                    img.writeMoments(dirFeatureVectors, "moments.txt", "imagespath.txt");

                        //image.close();
                    IJ.showProgress(1.0);
                    IJ.showStatus("");
                }
            }
        }

        GenericDialog gd = new GenericDialog("K-nearest neighbor search", IJ.getInstance());
        gd.addNumericField("Number of nearest neighbors (K):", 1, 0);
        gd.addNumericField("Distance Function: 1 (Manhatann) / 2 (Euclidean) / 3 (Infinity)", 1, 0);
        gd.showDialog();
        if (gd.wasCanceled())
            return;

        k = (int) gd.getNextNumber();
        int choose = (int) gd.getNextNumber();

        IJ.log("");
        IJ.log("Writing normalized moments in .TXT File...");

        List<Zernike> momentsList = new ArrayList<Zernike>();
        momentsList = Zernike.readMoments(dirFeatureVectors, "moments.txt", "imagespath.txt");


        normalize(momentsList); // normalizacao
        String dirMomentsNorm = dirFeatureVectors + File.separator + "moments-normalized";

        for(Zernike p: momentsList)
            p.writeMoments(dirMomentsNorm,"moments-norms.txt","imagespath.txt");

        IJ.log("");
        IJ.log("Computing and writing distance in .TXT File...");

        IJ.log("");
        IJ.log("Check distances below!!!");

        showKDistance(momentsList, k, choose);

        IJ.log("");
        IJ.log("Showing KNearests!");

        IJ.showProgress(1.0);
        IJ.showStatus("");
    }

    public void showKDistance(List<Zernike> momentsList, int k, int choose) {
        List<DistanceImg> dist = new ArrayList<>();

        // iterar sobre a lista de vetores momentos List<Zernike>
        // começamos a iterar com i=1 pois o elemento 0 da lista é a imagem de referencia
        for (int i = 1; i < momentsList.size(); i++) {
            switch (choose) {
                case 1:
                    // Adicionando um novo objeto DistanceImg(1º argumento, 2º argumento) na List<DistanceImg>
                    //  1º Argumento: Atribuindo a imagem da List<Zernike> para o novo objeto (DistanceImg) inserido na List<DistanceImg>
                    //  2º Argumento: Distancia Manhatann entre o vetor de referência (0) e o vetor de cada imagem (i) que estão na List<Zernike> e dentro da Classe Zernike (List<Double> moments)
                    dist.add(new DistanceImg(momentsList.get(i).getImg(), distManhatann(momentsList.get(0).getMoments(), momentsList.get(i).getMoments())));
                    break;
                case 2:
                    // Distancia Euclidiana
                    dist.add(new DistanceImg(momentsList.get(i).getImg(), distEuclidean(momentsList.get(0).getMoments(), momentsList.get(i).getMoments())));
                    break;
                case 3:
                    // Distancia Infinity
                    dist.add(new DistanceImg(momentsList.get(i).getImg(), distInfinity(momentsList.get(0).getMoments(), momentsList.get(i).getMoments())));
                    break;
                default:
                    return;
            }
        }

        Collections.sort(dist); // Ordenar a lista de distancias

        for (int i = 0; i < k; i++)
            (dist.get(i).getImg()).show(); // Mostrando as imagens - K mais próximas


        String distDir = dirFeatureVectors + File.separator + "distances";
        for (DistanceImg foreach : dist) {
            foreach.writeDistance(distDir, "distances-of-imgs.txt");
            IJ.log(foreach.toString());
        }
        IJ.log("");
    }

    public static void deleteDir(File dir){
        for(File infiles: dir.listFiles()){
            if(infiles.isDirectory())
                deleteDir(infiles);
            infiles.delete();
        }
    }

    public double distManhatann(List<Double> ref, List<Double> comparation){
        double sum = 0;
        for(int i=0; i < ref.size() ;i++)
            sum += Math.abs(ref.get(i) - comparation.get(i));
        return sum;
    }

    public double distEuclidean(List<Double> ref, List<Double> comparation){
        double sum = 0;
        for(int i=0; i < ref.size() ;i++)
            sum += Math.pow(ref.get(i) - comparation.get(i), 2);
        return Math.sqrt(sum);
    }

    public double distInfinity(List<Double> ref, List<Double> comparation){
        double temp, max = 0;
        for(int i=0; i < ref.size() ;i++){
            temp = Math.abs(ref.get(i) - comparation.get(i));
            if(temp > max)
                max = temp;
        }
        return max;
    }

    public void normalize(List<Zernike> list){
        double max, min;
        int imgPosition; // p/ iterar sobre as imagens
        int tamMoments = list.get(0).getMoments().size(); // tamanho do vetor de caracteristicas

        // iterando col sobre as colunas de momentos das imagens
        for(int col=0; col<tamMoments;col++) {
            List<Double> temp = new ArrayList<>();
            // iterando i ou seja a cada imagem (total de linhas = total de images)
            for (int i = 0; i < list.size(); i++)
                temp.add(list.get(i).getMoments().get(col)); // criando um array que contem o valor na mesma posição (col) de momento de cada imagem (i)

            // cópia do valor do array de momentos de cada imagem
            // apenas para conseguir o maior e menor valor ordenando-o
            List<Double> tempsorted = new ArrayList<>(temp);
            Collections.sort(tempsorted);
            min = tempsorted.get(0);
            max = tempsorted.get(temp.size() - 1);

            imgPosition = 0; // zerar na proxima iteração de colunas
            for (Double norm: temp){
                norm = (norm - min) / (max - min); // normalização do valor
                list.get(imgPosition).setPosMoment(col, norm); // atualizando a posiçao (col) do vetor trocando o valor pela norma (norm)
                imgPosition++;
            }
        }
    }
}

