import ij.*;
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;

import java.lang.Math;
import java.util.Comparator;


public class DistanceImg implements Comparable<DistanceImg> {
    private ImagePlus img;
    private double dist;

    DistanceImg(ImagePlus img, double dist) {
        this.img = img;
        this.dist = dist;
    }

    public ImagePlus getImg() {
        return img;
    }

    public double getDist() {
        return dist;
    }

    public int compareTo(DistanceImg next) {
        return Double.compare(this.dist, next.dist);
    }

    public String toStringImg() {
        final FileInfo fi = this.img.getOriginalFileInfo();
        return fi.fileName;
    }

    public String toString() {
        return String.format("%.5f", this.dist) + " " + toStringImg();
    }

    public void writeDistance(String dirDistances, String nameDistTXT) {
        File fileNewDir = new File(dirDistances);
        fileNewDir.mkdir();
        if (!dirDistances.endsWith(File.separator))
            dirDistances += File.separator;

        String pathDistTXT = dirDistances + nameDistTXT;
        File fileDist = new File(pathDistTXT);

        try {
            fileDist.createNewFile();
        } catch (Exception e) {
            IJ.log("ERRO AO CRIAR ARQUIVO: " + e);
        }
        try {
            BufferedWriter bwDist = new BufferedWriter(new FileWriter(pathDistTXT, true));

            bwDist.write(toString());

            bwDist.newLine();

            bwDist.close();

        } catch (Exception e) {
            IJ.log("ERRO DE ESCRITA: " + e);
        }
    }
}