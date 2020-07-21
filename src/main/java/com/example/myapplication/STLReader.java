package com.example.myapplication;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Package com.hc.opengl
 * Created by HuaChao on 2016/7/28.
 */
public class STLReader {
    private StlLoadListener stlLoadListener;
    private int patFaceCount = 6426;
    public Model parserBinStlInSDCard(String path) throws IOException {
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        return parserBinStl(fis);
    }

    public Model parserBinStlInAssets(Context context, String fileName) throws IOException {
        InputStream is = context.getAssets().open(fileName);
        return parserBinStl(is);
    }

    public Model parserAscStlInAssets(String filePath) throws IOException {
        Model model = new Model();
        model.setFacetCount(patFaceCount);
        File file = new File(filePath);
        float[] verts = new float[patFaceCount * 3 * 3];
        float[] vnorms = new float[patFaceCount * 3 * 3];
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String tmpString = null;
        int normNum = 0, vertNum = 0;
        while((tmpString = reader.readLine())!= null){
            String []tmpStrs = tmpString.split(" ");
            int tmpLen = tmpStrs.length;
            float x = 0, y = 0, z = 0;
            for(int i=0;i<tmpLen;i++){
                if(tmpStrs[i].equals("normal")){
                    x = Float.valueOf(tmpStrs[i+1]);
                    y = Float.valueOf(tmpStrs[i+2]);
                    z = Float.valueOf(tmpStrs[i+3]);
                    vnorms[9 * normNum] = x;
                    vnorms[9 * normNum + 1] = y;
                    vnorms[9 * normNum + 2] = z;
                    vnorms[9 * normNum + 3] = x;
                    vnorms[9 * normNum + 4] = y;
                    vnorms[9 * normNum + 5] = z;
                    vnorms[9 * normNum + 6] = x;
                    vnorms[9 * normNum + 7] = y;
                    vnorms[9 * normNum + 8] = z;
                    normNum++;
                }
                if(tmpStrs[i].equals("vertex")){
                    x = Float.valueOf(tmpStrs[i+3]);
                    y = Float.valueOf(tmpStrs[i+4]);
                    z = Float.valueOf(tmpStrs[i+5]);
                    verts[3 * vertNum] = x;
                    verts[3 * vertNum + 1] = y;
                    verts[3 * vertNum + 2] = z;
                    if(vertNum == 0){
                        model.minX = model.maxX = x;
                        model.minY = model.maxY = y;
                        model.minZ = model.maxZ = z;
                    }
                    else{
                        model.minX = Math.min(model.minX, x);
                        model.minY = Math.min(model.minY, y);
                        model.minZ = Math.min(model.minZ, z);
                        model.maxX = Math.max(model.maxX, x);
                        model.maxY = Math.max(model.maxY, y);
                        model.maxZ = Math.max(model.maxZ, z);
                    }
                    vertNum++;
                }
            }
        }
        reader.close();
        model.setVerts(verts);
        model.setVnorms(vnorms);
        //System.out.println("!!!!!!!!!!!!!!! "+vnorms.length+" "+verts.length);
        return model;
    }

    public Model parserBinStl(InputStream in) throws IOException {
        if (stlLoadListener != null)
            stlLoadListener.onstart();
        Model model = new Model();
        //前面80字节是文件头，用于存贮文件名；
        in.skip(80);

        //紧接着用 4 个字节的整数来描述模型的三角面片个数
        byte[] bytes = new byte[4];
        in.read(bytes);// 读取三角面片个数
        int facetCount = DensityUtil.byte4ToInt(bytes, 0);
        model.setFacetCount(facetCount);
        if (facetCount == 0) {
            in.close();
            return model;
        }
        // 每个三角面片占用固定的50个字节
        byte[] facetBytes = new byte[50 * facetCount];
        // 将所有的三角面片读取到字节数组
        in.read(facetBytes);
        //数据读取完毕后，可以把输入流关闭
        in.close();
        parseModel(model, facetBytes);
        if (stlLoadListener != null)
            stlLoadListener.onFinished();
        return model;
    }

    /**
     * 解析模型数据，包括顶点数据、法向量数据、所占空间范围等
     */
    private void parseModel(Model model, byte[] facetBytes) {
        int facetCount = model.getFacetCount();
        /**
         *  每个三角面片占用固定的50个字节,50字节当中：
         *  三角片的法向量：（1个向量相当于一个点）*（3维/点）*（4字节浮点数/维）=12字节
         *  三角片的三个点坐标：（3个点）*（3维/点）*（4字节浮点数/维）=36字节
         *  最后2个字节用来描述三角面片的属性信息
         * **/
        // 保存所有顶点坐标信息,一个三角形3个顶点，一个顶点3个坐标轴
        float[] verts = new float[facetCount * 3 * 3];
        // 保存所有三角面对应的法向量位置，
        // 一个三角面对应一个法向量，一个法向量有3个点
        // 而绘制模型时，是针对需要每个顶点对应的法向量，因此存储长度需要*3
        // 又同一个三角面的三个顶点的法向量是相同的，
        // 因此后面写入法向量数据的时候，只需连续写入3个相同的法向量即可
        float[] vnorms = new float[facetCount * 3 * 3];
        //保存所有三角面的属性信息
        short[] remarks = new short[facetCount];

        int stlOffset = 0;
        try {
            for (int i = 0; i < facetCount; i++) {
                if (stlLoadListener != null) {
                    stlLoadListener.onLoading(i, facetCount);
                }
                for (int j = 0; j < 4; j++) {
                    float x = DensityUtil.byte4ToFloat(facetBytes, stlOffset);
                    float y = DensityUtil.byte4ToFloat(facetBytes, stlOffset + 4);
                    float z = DensityUtil.byte4ToFloat(facetBytes, stlOffset + 8);
                    stlOffset += 12;

                    if (j == 0) {//法向量
                        // System.out.println("法向量：" + x + "," + y + "," + z);
                        vnorms[i * 9] = x;
                        vnorms[i * 9 + 1] = y;
                        vnorms[i * 9 + 2] = z;
                        vnorms[i * 9 + 3] = x;
                        vnorms[i * 9 + 4] = y;
                        vnorms[i * 9 + 5] = z;
                        vnorms[i * 9 + 6] = x;
                        vnorms[i * 9 + 7] = y;
                        vnorms[i * 9 + 8] = z;
                    } else {//三个顶点

                        verts[i * 9 + (j - 1) * 3] = x;
                        verts[i * 9 + (j - 1) * 3 + 1] = y;
                        verts[i * 9 + (j - 1) * 3 + 2] = z;

                        //记录模型中三个坐标轴方向的最大最小值
                        if (i == 0 && j == 1) {
                            model.minX = model.maxX = x;
                            model.minY = model.maxY = y;
                            model.minZ = model.maxZ = z;
                        } else {
                            model.minX = Math.min(model.minX, x);
                            model.minY = Math.min(model.minY, y);
                            model.minZ = Math.min(model.minZ, z);
                            model.maxX = Math.max(model.maxX, x);
                            model.maxY = Math.max(model.maxY, y);
                            model.maxZ = Math.max(model.maxZ, z);
                        }
                    }
                }
                short r = DensityUtil.byte2ToShort(facetBytes, stlOffset);
                stlOffset = stlOffset + 2;
                remarks[i] = r;
            }
        } catch (Exception e) {
            if (stlLoadListener != null) {
                stlLoadListener.onFailure(e);
            } else {
                e.printStackTrace();
            }
        }
        //将读取的数据设置到Model对象中
        model.setVerts(verts);
        model.setVnorms(vnorms);
        //System.out.println("!!!!!!!!!!!!!!! "+vnorms.length+" "+verts.length+" "+model.getFacetCount());
        model.setRemarks(remarks);

    }

    public static interface StlLoadListener {
        void onstart();

        void onLoading(int cur, int total);

        void onFinished();

        void onFailure(Exception e);
    }
}
