import ij.ImageStack
import ij.ImagePlus
import ij.process.ImageProcessor
import ij.gui.Roi
import java.awt.Rectangle
import ij.process.ImageStatistics
import ij.process.StackStatistics

#@ImagePlus(label="Image to be normalized", description="your image") myImage
#@Double (label="min value") newMin
#@Double (label="max value") newMax

#@StatusService ss
#@LogService ls

normalizeStack(myImage)
myImage.updateAndRepaintWindow()

	void normalizeStack(ImagePlus imp) {
        ImageStack stack = imp.getStack();
        int size = stack.getSize();
        double v;
        int width, height;
        int rx, ry, rw, rh;

        ImageProcessor ip = imp.getProcessor();
        width = ip.getWidth();
        height = ip.getHeight();
        Rectangle roi = ip.getRoi();
        byte[] mask = ip.getMaskArray();

        if (roi != null) {
            rx = roi.x;
            ry = roi.y;
            rw = roi.width;
            rh = roi.height;
        } else {
            rx = 0;
            ry = 0;
            rw = width;
            rh = height;
        }
     
        ss.showStatus("Finding min and max");
        ImageStatistics stats = new StackStatistics(imp);
        double roiMin = stats.min;
        double roiMax = stats.max;
        if (roiMax<=roiMin) roiMax = roiMin+1;
        double scale = (newMax-newMin) / (roiMax-roiMin);
        double offset = (newMin*roiMax - newMax*roiMin) / (roiMax-roiMin);
        ls.log(roiMin+"  "+roiMax+"  "+scale+"  "+offset);

        for (int slice=1; slice<=size; slice++) {
            ss.showStatus("Normalizing: "+slice+"/"+size);
            ss.showProgress(slice,size);
            ip = stack.getProcessor(slice);
            for (int y=0; y<height; y++) {
                int i = y * width + rx;
                for (int x=0; x<width; x++) {
                    v = ip.getPixelValue(x,y);
                    v = scale*v+offset;
                    ip.putPixelValue(x,y,v);
                    i++;
                }
            }
        }

    }

    void normalizeColorStack(ImagePlus imp) {
        ImageStack stack = imp.getStack();
        int size = stack.getSize();
        int v, r, g, b;
        int width, height;
        int rx, ry, rw, rh;

        ImageProcessor ip = imp.getProcessor();
        width = ip.getWidth();
        height = ip.getHeight();
        Rectangle roi = ip.getRoi();
        byte[] mask = ip.getMaskArray();

        if (roi != null) {
            rx = roi.x;
            ry = roi.y;
            rw = roi.width;
            rh = roi.height;
        } else {
            rx = 0;
            ry = 0;
            rw = width;
            rh = height;
        }

        // Find min and max

        int roiMinR = Integer.MAX_VALUE;
        int roiMinG = Integer.MAX_VALUE;
        int roiMinB = Integer.MAX_VALUE;
        int roiMaxR = -Integer.MAX_VALUE;
        int roiMaxG = -Integer.MAX_VALUE;
        int roiMaxB = -Integer.MAX_VALUE;

        for (int slice=1; slice<=size; slice++) {
            ss.showStatus("MinMax: "+slice+"/"+size);
			ss.showProgress(slice, size);            
            ip = stack.getProcessor(slice);
            if (mask == null) {
                for (int y=ry; y<(ry+rh); y++) {
                    int i = y * width + rx;
                    for (int x=rx; x<(rx+rw); x++) {
                        v = ip.getPixel(x,y);
                        r = (v&0xff0000)>>16;
                        g = (v&0xff00)>>8;
                        b = (v&0xff);
                        if (r<roiMinR) roiMinR = r;
                        if (g<roiMinG) roiMinG = g;
                        if (b<roiMinB) roiMinB = b;
                        if (r>roiMaxR) roiMaxR = r;
                        if (g>roiMaxG) roiMaxG = g;
                        if (b>roiMaxB) roiMaxB = b;
                        i++;
                    }
                }
            } else {
            	
            	ym = 0;
                for (int y=ry; y<(ry+rh); y++) {
                    int i = y * width + rx;
                    int im = ym * rw;
                    for (int x=rx; x<(rx+rw); x++) {
                        if(mask[im]!=0) {
                            v = ip.getPixel(x,y);
                            r = (v&0xff0000)>>16;
                            g = (v&0xff00)>>8;
                            b = (v&0xff);
                            if (r<roiMinR) roiMinR = r;
                            if (g<roiMinG) roiMinG = g;
                            if (b<roiMinB) roiMinB = b;
                            if (r>roiMaxR) roiMaxR = r;
                            if (g>roiMaxG) roiMaxG = g;
                            if (b>roiMaxB) roiMaxB = b;
                        }
                        i++; im++;
                    }
                    ym++;
                }
            }

        }

        if (roiMaxR<=roiMinR) roiMaxR = roiMinR+1;
        if (roiMaxG<=roiMinG) roiMaxG = roiMinG+1;
        if (roiMaxB<=roiMinB) roiMaxB = roiMinB+1;


        double scaleR = (newMax - newMin) / (roiMaxR - roiMinR);
        double scaleG = (newMax - newMin) / (roiMaxG - roiMinG);
        double scaleB = (newMax - newMin) / (roiMaxB - roiMinB);
        double offsetR = (newMin*roiMaxR - newMax*roiMinR) / (roiMaxR - roiMinR);
        double offsetG = (newMin*roiMaxG - newMax*roiMinG) / (roiMaxG - roiMinG);
        double offsetB = (newMin*roiMaxB - newMax*roiMinB) / (roiMaxB - roiMinB);

        for (int slice=1; slice<=size; slice++) {
            ss.showStatus("MinMax: "+slice+"/"+size);
            ss.showProgress(slice, size);
            ip = stack.getProcessor(slice);
            for (int y=ry; y<(ry+rh); y++) {
                int i = y * width + rx;
                for (int x=rx; x<(rx+rw); x++) {
                    v = ip.getPixel(x,y);
                    r = (v&0xff0000)>>16;
                    g = (v&0xff00)>>8;
                    b = (v&0xff);

                    r = (int) (scaleR*r+offsetR+0.5);
                    if (r<0) r=0;
                    if (r>255) r=255;
                    g = (int) (scaleG*g+offsetG+0.5);
                    if (g<0) g=0;
                    if (g>255) g=255;
                    b = (int) (scaleB*b+offsetB+0.5);
                    if (b<0) b=0;
                    if (b>255) b=255;

                    ip.putPixel(x,y,(r<<16)|(g<<8)|b);
                    i++;
                }
            }

        }
    }
