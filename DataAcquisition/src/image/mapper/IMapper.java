package image.mapper;

import java.awt.image.RenderedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.log4j.Logger;

@SuppressWarnings({"unchecked"})
public class IMapper {

	static Logger logger = Logger.getLogger(IMapper.class.getName());

	private final static IntWritable one = new IntWritable(1);
	private static Text word = new Text();
	private static Map<String, List<String>> urls = new HashMap<String, List<String>>();

	public static class ImageMapper extends Mapper<Object, Text, Text, IntWritable>{
		
		@Override
		public void setup(Context context) throws IOException,
		InterruptedException {
			Path urlSerializedMapPath = new Path("hdfs://localhost:9000/user/Shank/urlMap.ser");
			FileSystem hdfs = FileSystem.get(new Configuration());

			DataInputStream dis = new DataInputStream(hdfs.open(urlSerializedMapPath));
			ObjectInputStream ois = new ObjectInputStream(dis);
			try {
				urls = (HashMap<String, List<String>>)ois.readObject();
			} catch (Exception e) {
				word.set(e.getMessage());
				context.write(word, one);
			}

			ois.close();
			dis.close();
		}

		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

			List<String> places = Arrays.asList(value.toString().split("\\,"));
			try {
				downloadImages(urls, places);
			} catch (Exception e) {
				word.set(e.getMessage());
				context.write(word, one);
			}
		}
	}

	public static void downloadImages(Map<String, List<String>> urls, List<String> places)
			throws Exception {

		logMe("Starting image downloads....");

		RenderedImage image = null;
		File outputfile = null;
		URLConnection con = null;
		int count = 0;
		URL imageUrl = null;
		InputStream in = null;

		for (String place : places) {

			logMe("Downloading images for: " + place);

			count = 1;
			new File("/Users/Shank/images").mkdir();
			new File("/Users/Shank/images/" + place.replaceAll("\\/", "-")).mkdir();

			for (String url : urls.get(place)) {
				if (count == 510) {
					break;
				}

				try {
					imageUrl = new URL(url);
					con = imageUrl.openConnection();
					con.setConnectTimeout(10000);
					con.setReadTimeout(20000);
					in = con.getInputStream();

					image = ImageIO.read(in);
					outputfile = new File("/Users/Shank/images/"
							+ place.replaceAll("\\/", "-") + "/" + count + "."
							+ url.split("\\.")[url.split("\\.").length - 1]);
					ImageIO.write(image,
							url.split("\\.")[url.split("\\.").length - 1],
							outputfile);
				} catch (Exception e) {
					logErr(url + "-" + e.getMessage());
					continue;
				}
				count++;
			}

			logMe("Image downloads completed for: " + place + " :: Count: "
					+ count);
		}
		logMe("All image downloads complete!!");
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		conf.set("mapred.child.java.opts", "-Xmx2048m -Xss1024m");
		
		GenericOptionsParser cmdArgsParser = new GenericOptionsParser(conf, args);
		String[] cmdArgs = cmdArgsParser.getRemainingArgs();

		if (cmdArgs.length != 2) {
			System.err.println("Usage: wordcount <in> <out>");
			System.exit(2);
		}

		Job job = Job.getInstance(conf, "Data Acquisition");
		job.setJarByClass(IMapper.class);
		job.setMapperClass(ImageMapper.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		FileInputFormat.addInputPath(job, new Path(cmdArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(cmdArgs[1]));

		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}

	public static void logMe(String msg) {
		logger.info(msg);
	}

	public static void logErr(String msg) {
		logger.error(msg);
	}
}
