import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.fazecast.jSerialComm.*;
public class SNSPOST{
	private static SerialPort comPort;
	private static BufferedReader input;
	private static String key="";
	public static void main(String[]args){
		String loginUrl="http://1.240.170.144:8080/api/users/login?userId=mhnet&password=111111";
		String cookie=login(loginUrl);
		String readUHSDM="",originalMessage="",setMessage="",replaceMessage="";
		input=new BufferedReader(new InputStreamReader(System.in));
		setSerial();
		while(true) {
			System.out.println("====메뉴====");
			System.out.println("0. 서버 재접속");
			System.out.println("1. 포트 재설정");
			System.out.println("2. 메시지 전송");
			System.out.println("3. 프로그램 종료");
			System.out.println("==========");
			try{
				key=input.readLine();
			}catch(IOException e){
				e.printStackTrace();
			}
			if(key.equalsIgnoreCase("0")){
				login(loginUrl);
			}else if(key.equalsIgnoreCase("1")){
				if(comPort.isOpen()){
					try{
						comPort.closePort();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				setSerial();
			}else if(key.equalsIgnoreCase("2")){
				try{
					readUHSDM=readSerial();
				}catch(Exception e){
					System.out.println("UHSDM 메시지 읽기 오류");
					readUHSDM="Rx >>> b>2:10> 01 0A 02 0F AA BB CC DD EE FF 56 BA";
				}
				try{
					originalMessage=URLEncoder.encode(readUHSDM,"UTF-8");
				}catch(Exception e){
					System.out.println("원본 메시지 인코딩 오류");
					originalMessage="Rx+%3e%3e%3e+b%3e2%3a10%3e+01+0A+02+0F+AA+BB+CC+DD+EE+FF+56+BA";
				}
				System.out.println("원본 메시지 "+post(originalMessage,cookie));
				try{
					setMessage=URLEncoder.encode(setMessage(readUHSDM),"UTF-8");
					replaceMessage=setMessage.replace("NEXTLINE","%0D%0A");
				}catch(UnsupportedEncodingException e){
					System.out.println("변환 메시지 인코딩 오류");
					replaceMessage="%EA%B8%B0%EA%B8%B0+%EC%95%84%EC%9D%B4%EB%94%94+%3A+01%0d%0a%EC%A0%84%EC%86%A1+%EB%A7%A4%EC%B2%B4+%3A+%EC%B2%AD%EC%83%89%EA%B0%80%EC%8B%9C%EA%B4%91%0d%0a%EC%88%98%EC%98%A8+%3A+AA%0d%0a%EC%88%98%EC%8B%AC+%3A+BB%0d%0a%EA%B4%91%EB%8F%84+%3A+CC%0d%0a%EC%A3%BC%EB%B3%80%EC%83%89+%3A+DD%0d%0a%EB%82%B4%EB%B6%80%EC%98%A8%EB%8F%84+%3A+EE%0d%0a%EB%82%B4%EB%B6%80%EC%8A%B5%EB%8F%84+%3A+FF";
					e.printStackTrace();
				}
				System.out.println("변환 메시지 "+post(replaceMessage,cookie));
			}else if(key.equalsIgnoreCase("3")){
				break;
			}else{
				System.out.println("잘못된 입력입니다. 메뉴내의 번호를 입력해주세요.");
			}
		}
		try{
			input.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		if(comPort.isOpen()){
			try{
				comPort.closePort();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	private static String login(String loginUrl){
		HttpURLConnection con=connect(loginUrl);
		try{
			con.setRequestMethod("PUT");
			int responseCode=con.getResponseCode();
			if(responseCode==HttpURLConnection.HTTP_OK){
//				System.out.println(readBody(con.getInputStream()));
				String cookie=con.getHeaderField("set-cookie");
				if(cookie!=null){
					System.out.println("서버 접속 성공");
					return cookie;
				}else{
//					System.out.println("NO Cookie");
					System.out.println("서버 접속 실패");
					return "JSESSIONID=CE016FB1EA608429708FA6B8EBDB0D93; Path=/; HttpOnly";
				}
			}else{
				System.out.println(readBody(con.getErrorStream()));
				return "JSESSIONID=CE016FB1EA608429708FA6B8EBDB0D93; Path=/; HttpOnly";
			}
		}catch(IOException e){
//			throw new RuntimeException("API 요청과 응답 실패",e);
			System.out.println("API 요청과 응답 실패");
			return "JSESSIONID=CE016FB1EA608429708FA6B8EBDB0D93; Path=/; HttpOnly";
		}finally{
			con.disconnect();
		}
	}
	private static String post(String message,String cookie){
		String postUrl="http://1.240.170.144:8080/api/posts?type=Story&title=test112&vic=10&latitude=10&longtitude=20&accountType=Divememory&note=%0D%0A"+message;
		HttpURLConnection con=connect(postUrl);
		try{
			con.setRequestMethod("POST");
			con.setRequestProperty("cookie",cookie);
			int responseCode=con.getResponseCode();
			if(responseCode==HttpURLConnection.HTTP_OK){
//				return readBody(con.getInputStream());
				return "전송 성공";
			}else{
				return readBody(con.getErrorStream());
			}
		}catch(IOException e){
//			throw new RuntimeException("API 요청과 응답 실패",e);
			return "API 요청과 응답 실패";
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
		try(BufferedReader lineReader=new BufferedReader(streamReader)){
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
	private static void setSerial(){
		if(SerialPort.getCommPorts().length==0){
			System.out.println("연결된 장치가 없습니다.");
			return;
		}
		System.out.println("====장치 목록====");
		for(int i=0;i<SerialPort.getCommPorts().length;i++) {
			System.out.println(i+". "+SerialPort.getCommPorts()[i].getDescriptivePortName());
		}
		System.out.println("==========");
		try{
			key=input.readLine();
		}catch(IOException e){
			e.printStackTrace();
		}
		try{
			int portNum=Integer.parseInt(key);
			comPort=SerialPort.getCommPorts()[portNum];
		}catch(NumberFormatException e){
			System.out.println("숫자만 입력해주세요.");
			comPort=SerialPort.getCommPorts()[0];
		}catch(Exception e){
    		System.out.println("장치 연결 실패");
    	}
		comPort.setBaudRate(115200);
		comPort.setNumDataBits(8);
		comPort.setNumStopBits(1);
		comPort.openPort();
		comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING,0,0);
	}
	private static String readSerial(){
		String read="";
		try{
//			SerialPort comPort=SerialPort.getCommPort("/dev/ttyACM0");
//    		comPort.setBaudRate(115200);
//    		comPort.setNumDataBits(8);
//    		comPort.setNumStopBits(1);
//    		comPort.openPort();
//    		comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING,0,0);
    		BufferedReader bf=new BufferedReader(new InputStreamReader(comPort.getInputStream()));
    		read=bf.readLine();
    		bf.close();
//    		comPort.closePort();
    	}catch(IOException e){
    		System.out.println("시리얼 읽기 오류");
    		read="Rx >>> b>2:10> 01 0A 02 0F AA BB CC DD EE FF 56 BA";
    		e.printStackTrace();
    	}
    	return read;
    }
	private static String setMessage(String message){
		String result="",split="",light="";
		try{
			split=message.split(">")[5];
			if(split.split(" ")[3].equalsIgnoreCase("01")){
				light="적외선";
			}else if(split.split(" ")[3].equalsIgnoreCase("02")){
				light="청색가시광";
			}else if(split.split(" ")[3].equalsIgnoreCase("03")){
				light="적색가시광";
			}else{
				light="가시광";
			}
			result="기기 아이디 : "+split.split(" ")[1]+"NEXTLINE"
					+"전송 매체 : "+light+"NEXTLINE"
					+"수온 : "+split.split(" ")[5]+"NEXTLINE"
					+"수심 : "+split.split(" ")[6]+"NEXTLINE"
					+"광도 : "+split.split(" ")[7]+"NEXTLINE"
					+"주변색 : "+split.split(" ")[8]+"NEXTLINE"
					+"내부온도 : "+split.split(" ")[9]+"NEXTLINE"
					+"내부습도 : "+split.split(" ")[10];
		}catch(Exception e){
			System.out.println("잘못된 형식입니다.");
			result="기기 아이디 : 01NEXTLINE전송 매체 : 청색가시광NEXTLINE수온 : AANEXTLINE수심 : BBNEXTLINE광도 : CCNEXTLINE주변색 : DDNEXTLINE내부온도 : EENEXTLINE내부습도 : FF";
		}
		return result;
	}
}
