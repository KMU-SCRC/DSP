import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import com.fazecast.jSerialComm.*;
public class SNSPOST {
    public static void main(String[] args) {
    	String loginUrl="http://1.240.170.144:8080/api/users/login?userId=mhnet&password=111111";
    	String cookie=login(loginUrl);
    	String readUHSDM=readSerial();
    	String postUrl="http://1.240.170.144:8080/api/posts?type=Story&title=test112&vic=10&latitude=10&longtitude=20&accountType=Divememory&note=test-"+readUHSDM;
    	String responseBody=post(postUrl,cookie);
        System.out.println(responseBody);
    }
    private static String login(String loginUrl) {
        HttpURLConnection con=connect(loginUrl);
        try{
            con.setRequestMethod("PUT");
            int responseCode=con.getResponseCode();
            if(responseCode==HttpURLConnection.HTTP_OK){
            	System.out.println(readBody(con.getInputStream()));
            	String cookie=con.getHeaderField("set-cookie");
            	if(cookie!=null) {
            		return cookie;
            	}else {
            		System.out.println("NO Cookie");
            		return "JSESSIONID=CE016FB1EA608429708FA6B8EBDB0D93; Path=/; HttpOnly";
            	}
            }else{
            	System.out.println(readBody(con.getErrorStream()));
                return "JSESSIONID=CE016FB1EA608429708FA6B8EBDB0D93; Path=/; HttpOnly";
            }
        }catch(IOException e){
            throw new RuntimeException("API 요청과 응답 실패",e);
        }finally{
            con.disconnect();
        }
    }
    private static String post(String postUrl,String cookie) {
        HttpURLConnection con=connect(postUrl);
        try {
            con.setRequestMethod("POST");
            con.setRequestProperty("cookie", cookie);
            int responseCode=con.getResponseCode();
            if(responseCode==HttpURLConnection.HTTP_OK){
                return readBody(con.getInputStream());
            }else{
                return readBody(con.getErrorStream());
            }
        }catch(IOException e){
            throw new RuntimeException("API 요청과 응답 실패",e);
        }finally{
            con.disconnect();
        }
    }
    private static HttpURLConnection connect(String apiUrl){
        try{
            URL url=new URL(apiUrl);
            return (HttpURLConnection)url.openConnection();
        }catch(MalformedURLException e){
            throw new RuntimeException("API URL이 잘못되었습니다. : "+apiUrl,e);
        }catch(IOException e){
            throw new RuntimeException("연결이 실패했습니다. : "+apiUrl,e);
        }
    }
    private static String readBody(InputStream body){
        InputStreamReader streamReader=new InputStreamReader(body,StandardCharsets.UTF_8);
        try (BufferedReader lineReader=new BufferedReader(streamReader)){
            StringBuilder responseBody=new StringBuilder();
            String line;
            while((line=lineReader.readLine())!=null){
                responseBody.append(line);
            }
            return responseBody.toString();
        }catch(IOException e){
            throw new RuntimeException("API 응답을 읽는데 실패했습니다.",e);
        }
    }
    private static String readSerial(){
    	String read="";
    	try{
    		SerialPort comPort=SerialPort.getCommPort("/dev/pts/14");
			comPort.setBaudRate(115200);
			comPort.setNumDataBits(8);
			comPort.setNumStopBits(1);
			comPort.openPort();
			comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING,0,0);
			BufferedReader bf=new BufferedReader(new InputStreamReader(comPort.getInputStream()));
			read=bf.readLine();
			bf.close();
			comPort.closePort();
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    	return read;
    }
}