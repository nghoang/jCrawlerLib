import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.NameValuePair;

import com.ngochoang.CrawlerLib.PostFileData;
import com.ngochoang.CrawlerLib.WebClientX;

public class Tester {
	public static void main(String[] args) {
		try {
			 WebClientX client = new WebClientX();
			 Vector<NameValuePair> formparams = new Vector<NameValuePair>();
			 Vector<PostFileData> uploadFiles = new Vector<PostFileData>();
			 uploadFiles.add(new PostFileData("ts.txt","C:\\tesseract.exe"));
			 System.out.println(client.PostMethod("http://pacstudy.com/index.php/ajax/test",
			 formparams, uploadFiles));

//			HttpClient httpclient = new DefaultHttpClient();
//			httpclient.getParams().setParameter(
//					CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
//
//			HttpPost httppost = new HttpPost("http://pacstudy.com");
//			File file = new File("c:/captcha_sc2606222.jpg");
//
//			MultipartEntity mpEntity = new MultipartEntity();
//			ContentBody cbFile = new FileBody(file, "image/jpeg");
//			mpEntity.addPart("userfile", cbFile);
//
//			httppost.setEntity(mpEntity);
//			System.out
//					.println("executing request " + httppost.getRequestLine());
//			HttpResponse response = httpclient.execute(httppost);
//			HttpEntity resEntity = response.getEntity();
//
//			System.out.println(response.getStatusLine());
//			if (resEntity != null) {
//				System.out.println(EntityUtils.toString(resEntity));
//			}
//			if (resEntity != null) {
//				resEntity.consumeContent();
//			}
//
//			httpclient.getConnectionManager().shutdown();

		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.getLogger(Tester.class.getName())
					.log(Level.SEVERE, null, ex);
		}
	}

}
