import ij.*;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.io.Opener;
import ij.io.SaveDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.io.FileInfo;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math; // Utilizar funções: Math.min(), Math.round(), Math.sin(), Math.cos(), Math.toRadians(), Math.sqrt(),  Math.pow()

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Zernike{
    private ImagePlus img;
    private List<Double> moments;

    Zernike(ImagePlus img){
        this.img = img;
        this.moments = new ArrayList<>();
    }

    public ImagePlus getImg() {
        return img;
    }

    public List<Double> getMoments() {
        return moments;
    }

    public void setMoments(List<Double> newMoments) {
        this.moments = newMoments;
    }

    public void setPosMoment(int index, double value){
        this.moments.set(index, value);
    }

    public void zernikeMoments(int degree){
        ImageAccess imgAcess = new ImageAccess(img.getProcessor());
        int nx = imgAcess.getWidth();
        int ny = imgAcess.getHeight();
        // foi-se utilizado o -1 abaixo pois os pixels começam em 0
        int midx = (int) Math.round((double) nx/2) - 1; // round() caso a largura seja um número impar
        int midy = (int) Math.round((double) ny/2) - 1; // round() caso a altura seja um número impar
        int dx, dy, px, py;
        double moment=0;
        double polarMoment;

        for(int n=0; n <= degree; n++) {
            for (int l=0; l <= n; l++) {
                if((n-l)%2 == 0) {  // se n - l for um número par
                    for (int r=1; r <= Math.min(midx,midy); r++) { // iterar enquanto o raio for menor ou igual do menor lado
                        double rnl = Rnl(n,l,r);
                        for(int theta=0; theta < 360; theta++) {
                            dx = (int) Math.round(r * Math.cos(Math.toRadians(theta)));
                            dy = (int) Math.round(r * Math.sin(Math.toRadians(theta)));
                            px = midx + dx;
                            py = midy + dy;
                            moment += Vnl(n,l,theta,rnl) * imgAcess.getPixel(px,py) * rnl;
                        }
                    }
                    polarMoment = ((n + 1)/Math.PI) * moment;
                    this.moments.add(polarMoment);
                }
                moment = 0;
            }
        }
    }

    private double Vnl(int n, int l, int theta, double rnl){
        return Math.sqrt(Math.pow(rnl * Math.cos( Math.toRadians(l*theta))), 2) + Math.pow( (rnl * Math.sin( Math.toRadians(l*theta))) , 2));
    }

    private double Rnl(int n, int l, int r){
        double rnl = 0;
        for(int s=0; s<=((n-l)/2);  s++){
            rnl += Math.pow(-1, s) * Math.pow(r, (n-(2*s))) * (double) (fatorial(n - s) / (fatorial(s) * fatorial(((n+l)/2)-s) * fatorial(((n-l)/2)-s)));
        }
        return rnl;
    }

    public int fatorial(int n){
        int fat = 1;
        if(n==0)
            return 1;
        else{
            for(int i = 1; i <= n; i++)
                fat = fat * i;
        }
        return fat;
    }

    public String toStringPathImage(){
        final FileInfo fi = img.getOriginalFileInfo();
        String pathToImage = fi.directory + fi.fileName;

        return pathToImage;
    }

    public String toStringMoments(){
        //final FileInfo fi = img.getOriginalFileInfo();
        //String pathToFile = fi.directory + fi.fileName;
        StringBuilder s = new StringBuilder();
        String sep = " ";

        //s.append(pathToFile);
        //s.append(sep);
        for(int i = 0; i<moments.size(); i++) {
            s.append(String.format("%.10f", moments.get(i))); // Definir a precisão
            s.append(sep);
        }
        return s.toString();
    }

    public void writeMoments(String newDirName, String fileNameMoments, String fileNameImages){
        // Criação do diretório "feature-vectors" para colocar os momentos em .txt
        File fileNewDir = new File(newDirName);
        fileNewDir.mkdir();
        if (!newDirName.endsWith(File.separator))
            newDirName += File.separator;

        // Criação do arquivo fileNameMoments("moments.txt") no diretorio "feature-vectors"
        String pathMomentsTXT = newDirName + fileNameMoments;
        File fileMoments = new File(pathMomentsTXT);

        // Criação do arquivo fileNamePathToImages("imagespath.txt")
        String pathImagesTXT = newDirName + fileNameImages;
        File fileImages = new File(pathImagesTXT);

        try {
            fileMoments.createNewFile();
            fileImages.createNewFile();
        }
        catch(Exception e) {
            IJ.log("ERRO AO CRIAR ARQUIVO: " + e);
        }
        try {
            // Escrever no final do arquivo "moments.txt"
            BufferedWriter bwMoments = new BufferedWriter(new FileWriter(pathMomentsTXT, true));
            BufferedWriter bwImages = new BufferedWriter(new FileWriter(pathImagesTXT, true));
            bwMoments.write(toStringMoments());
            bwImages.write(toStringPathImage());
            bwMoments.newLine();
            bwImages.newLine();
            bwMoments.close();
            bwImages.close();
        } catch (Exception e) {
            IJ.log("ERRO DE ESCRITA: " + e);
        }
        // FIM da escrita dos momentos.txt
    }

    public static List<Zernike> readMoments(String DirName, String fileNameMoments, String fileNamePathToImages) {

        if (!DirName.endsWith(File.separator))
            DirName += File.separator;
        // Caminho do arquivo fileNameMoments("moments.txt") do diretorio DirName("feature-vectors")
        String pathMomentsTXT = DirName + fileNameMoments;
        // Caminho do arquivo fileNamePathToImages("imagespath.txt")
        String pathImagesTXT = DirName + fileNamePathToImages;


        // Criando a lista para incluir os elementos dos arquivos que estão sendo lidos
        List<Zernike> zernikeList = new ArrayList<>();

        // Ler os arquivos
        try (BufferedReader brMoments = new BufferedReader(new FileReader(pathMomentsTXT)); BufferedReader brImage = new BufferedReader(new FileReader(pathImagesTXT));){

            String lineMoments;
            String lineImage;

            while ((lineMoments = brMoments.readLine()) != null && (lineImage = brImage.readLine()) != null) {

                ImagePlus newImg = new Opener().openImage(lineImage); // apenas armazenando a imagem
                Zernike tempZernike = new Zernike(newImg);
                List<Double> tempMoments = new ArrayList<>();

                String[] partMoments = lineMoments.split(" "); // split para separar cada momento em um array de string
                for (int i = 0; i < partMoments.length; i++) { // pegando cada elemento do vetor de string
                    tempMoments.add(Double.parseDouble((partMoments[i]).replace(",", "."))); // transformando em double o elemento e adicionando na lista de momentos
                }
                tempZernike.setMoments(tempMoments); //carregando os momentos no temporario

                // Adicionando o objeto Zernike (imagem + momentos lidos) à lista
                zernikeList.add(tempZernike);
            }
            brMoments.close();
            brImage.close();
        } catch (Exception e) {
            IJ.log("ERRO DE LEITURA: " + e);
        }

        return zernikeList;
    }
}



