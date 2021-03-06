package ru.detone_studio.radio_set.server.States;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.Array;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Enumeration;

import ru.detone_studio.radio_set.server.GameStateManager;

import static com.badlogic.gdx.math.MathUtils.random;

/**
 * Created by Voland on 29.10.2017.
 */

public class PlayState extends State {
    private OrthographicCamera camera;


    static final int multi = 1;
    //static final int samples = 44100;
    static final int samples = 22050;
    static boolean isMono = true;
    static boolean closed_block;
    final short[] data2 = new short[samples * 1];
    static final Array<short[]> data = new Array<short[]>();
    static final Array<short[]> data_rcv = new Array<short[]>();
    static final  Array<Boolean> blocked = new Array<Boolean>();
    Sprite send = new Sprite(new Texture(Gdx.files.internal("send.png")));
    Sprite send2 = new Sprite(new Texture(Gdx.files.internal("send.png")));
    private BitmapFont FontRed1=new BitmapFont();
    int local_i;
    int sync_i,sync_j;
    int info_i=0;
    //short[] data = new short[samples*1];


    static final AudioRecorder recorder = Gdx.audio.newAudioRecorder(samples, isMono);;
    static final AudioDevice player = Gdx.audio.newAudioDevice(22050, isMono);

    static boolean output_signal=false;
    int who_rec;
    int dynamic_port=9001;
    String ip_adress="192.168.2.2";
    //String ip_adress="185.132.242.124";
    int clients_online=0;

    static boolean touched=false;
    float current_dt=0.0f;

    //Буффер отправки
    int size_of_system_buffer=4096*2;
    byte system_buffer[]=new byte[size_of_system_buffer];

    int current_sends_block;
    Array<Short> current_block=new Array<Short>();





    public PlayState(GameStateManager gsm) {
        super(gsm);
        closed_block=false;
        camera = new OrthographicCamera();
        camera.setToOrtho(false,480,800);
        send.setPosition(10,10);
        send2.setPosition(10,600);

                //play_snd();
        //save_snd();
        Input.TextInputListener listener = new Input.TextInputListener() {
            @Override
            public void input(String text) {
                ip_adress=text;
                recv_msg(9000,true);
                clean_up();
            }

            @Override
            public void canceled() {
                recv_msg(9000,true);
            }
        };

        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()&&inetAddress instanceof Inet4Address) {
                        String ipAddress=inetAddress.getHostAddress().toString();
                        System.out.println("IP address: "+ipAddress);
                        ip_adress=ipAddress;
                    }
                }
            }
        } catch (SocketException ex) {
           // Log.e("Socket exception in GetIP Address of Utilities", ex.toString());
        }
        //Gdx.input.getTextInput(listener, "Enter server adress","","");
        Gdx.input.getTextInput(listener, "Enter server adress", ip_adress, "");



        //recv_msg(9000,true);
        //recv_msg(9998);
        //recv_msg(9997,false);



    }

    @Override
    protected void handleInput() {
        Vector3 touchPos = new Vector3();
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPos);

        if (Gdx.input.isTouched()){
            if (send2.getBoundingRectangle().contains(touchPos.x,touchPos.y)) {
            }
            else

            if (send.getBoundingRectangle().contains(touchPos.x,touchPos.y)){
                //send_msg();

            }
            else {
                current_dt = 0;
                touched = true;
            }
        }
       /* if (Gdx.input.justTouched()) {



            if (send2.getBoundingRectangle().contains(touchPos.x,touchPos.y))
            {
                System.out.println("Send 2");
                data.add(new short[samples * 1]);
                for (int i=1;i<22050;i++) {
                    data.get(data.size-1)[i]= (short) (random.nextInt(64000)-32000);
                    //if (i>=44099){
                    //   System.out.println("unblock");
                    //    blocked.add(false);
                    // }
                }
                System.out.println("buffer 0 "+data.get(data.size-1)[0]);
                System.out.println("buffer 1 "+data.get(data.size-1)[1]);
                System.out.println("buffer 2 "+data.get(data.size-1)[2]);
                System.out.println("buffer 3 "+data.get(data.size-1)[3]);
                System.out.println("buffer 4 "+data.get(data.size-1)[4]);
            }
            else

            if (send.getBoundingRectangle().contains(touchPos.x,touchPos.y)){
                send_msg();

            }

            //System.out.println("New Tread");
        }*/
    }

    @Override
    public void update(float dt) {

        current_dt += dt;
        if (current_dt>1.0f/multi){
            handleInput();

        }

    }

    @Override
    public void render(SpriteBatch sb) {
        sb.setProjectionMatrix(camera.combined);

        //send.draw(sb);
        //send2.draw(sb);

        FontRed1.draw(sb,"Info_i: "+info_i,10,300);
        FontRed1.draw(sb,"Sync_j: "+sync_j,10,270);
        FontRed1.draw(sb,"Clients_online: "+clients_online,10,400);
        FontRed1.draw(sb,"Server working on: "+ip_adress,10,420);


    }

    @Override
    public void dispose() {

    }

    public void send_msg(){
        new Thread(new Runnable() {
            @Override
            public void run () {

                SocketHints hints = new SocketHints();
                hints.socketTimeout = 5000;
                Socket client = Gdx.net.newClientSocket(Net.Protocol.TCP, "192.168.1.196", 9999, hints);

                try {

                    //client.getOutputStream().write("PING\n".getBytes());
                    ByteBuffer buffer2 = ByteBuffer.allocate(2);
                    byte buffer[] = new byte[392];
                    sync_j = 0;
                    for (int i = 1; i <= (225); i++) {
                        for (int j = 1; j < (99); j++) {

                            //System.out.println("sync_i"+sync_j);

                            buffer2.putShort(data.get(0)[sync_j]);
                            buffer[j * 2 - 2] = buffer2.get(0);
                            buffer[j * 2 - 1] = buffer2.get(1);
                            buffer2.clear();

                            sync_j++;
                        }

                        client.getOutputStream().write(buffer);
                    }
                    //String response = new BufferedReader(new InputStreamReader(client.getInputStream())).readLine();
                    //Gdx.app.log("PingPongSocketExample", "got server message: " + response);
                    data.removeIndex(0);

                    //data_rcv.clear();

                    //blocked.removeIndex(0);

                } catch (IOException e) {
                    Gdx.app.log("PingPongSocketExample", "an error occured", e);
                }
            }
        }).start();

    }

    public void clean_up(){
        System.out.println("start clean_up thread");
        new Thread(new Runnable() {

            @Override
            public void run() {
                int run;
                while (true){
                    if (data_rcv.size>0) {
                        System.out.println("start clean_up");
                        run = 0;
                        for (Short miniblock : current_block) {
                            if (miniblock > 0) {
                                run++;
                            }
                        }
                        System.out.println("run= "+run);
                        if (run >= clients_online-1) {
                            System.out.println("delete");
                            for (int k=0;k<clients_online;k++){
                              current_block.set(k, (short) 0);
                            }

                            System.out.println("Clean_up d.size=" + data_rcv.size);
                            data_rcv.removeIndex(0);
                            //data_rcv.clear();

                            blocked.removeIndex(0);
                        }
                        try {
                            Thread.sleep(800);
                        } catch (InterruptedException e) {
                           // e.printStackTrace();
                        }
                    }
                    else
                    {
                        System.out.println("d.size=" + data_rcv.size);
                        try {
                            Thread.sleep(800);
                        } catch (InterruptedException e) {
                           // e.printStackTrace();
                        }
                    }

                }
            }
        }).start();
    }

    public void recv_msg(final int mimport, final boolean Authorization_Port){

        new Thread(new Runnable() {
            @Override
            public void run () {
                boolean close_socket=true;//заглушка для открытых сокетов
                ServerSocket server;
                java.net.ServerSocket serverSocket2 = null;
                SocketHints hints1;
                ServerSocketHints hints;
                byte buffer[] = new byte[196];
                int number_block=0;

                //Порт авторизации
                if (Authorization_Port) {
                    try {
                        hints = new ServerSocketHints();
                        hints1 = new SocketHints();
                        hints1.socketTimeout = 5000;

                        server = Gdx.net.newServerSocket(Net.Protocol.TCP, ip_adress, mimport, hints);
                        //server = Gdx.net.newServerSocket(Net.Protocol.TCP, "185.132.242.124", 9999, hints);

                    } catch (Exception ignore) {
                        server = null;
                        hints = null;
                        hints1 = null;
                    }
                    while (close_socket) {
                        try {
                            System.out.println("conected? _auth: "+mimport);
                            Socket client = server.accept(hints1);

                            System.out.println("accept: "+client.getRemoteAddress());

                            byte hand_shake_buffer[] = new byte[2];
                            client.getInputStream().read(hand_shake_buffer);
                            System.out.println("hand_shake_buffer[0] : " + mimport + " " + hand_shake_buffer[0]);
                            //Если все хорошо клиент спрашивает порт
                            if (hand_shake_buffer[0]==11){
                                //ответ 21 что все хорошо
                                hand_shake_buffer[0]=21;
                                client.getOutputStream().write(hand_shake_buffer);
                                System.out.println("send answer " + hand_shake_buffer[0]);
                                dynamic_port-=9000;

                                //отправка свободного порта(нужно доработать)
                                hand_shake_buffer[0]= (byte) dynamic_port;
                                dynamic_port+=9000;
                                recv_msg(dynamic_port,false);
                                dynamic_port++;
                                client.getOutputStream().write(hand_shake_buffer);
                                System.out.println("send port " + hand_shake_buffer[0]);
                                //client.dispose();

                            }


                        } catch (Exception e) {
                            Gdx.app.log("PingPongSocketExample", "Error Auth", e);

                        }

                    }
                }
                else
                {

                    try {
                        hints = new ServerSocketHints();

                        hints1 = new SocketHints();
                        hints1.socketTimeout = 5000;



                        clients_online++;
                        current_block.add((short) 0);
                        number_block =  current_block.size-1;



                        //int mini_buffer_size=45056;
                        //int buffer_size=size_of_system_buffer;
                        //hints.receiveBufferSize=mini_buffer_size;

                        //byte ss=127;
                        //hints1.trafficClass=ss;
                        //hints1.sendBufferSize=buffer_size;
                        //hints1.receiveBufferSize=buffer_size;
                        //hints1.sendBufferSize=mini_buffer_size;
                        //hints1.receiveBufferSize=mini_buffer_size;
                        //hints1.connectTimeout=5000;

                        //server = Gdx.net.newServerSocket(Net.Protocol.TCP, ip_adress, mimport, hints);
                        serverSocket2=new java.net.ServerSocket(mimport);
                        System.out.println("Waiting for a client...");
                        //server = Gdx.net.newServerSocket(Net.Protocol.TCP, "185.132.242.124", 9999, hints);

                    } catch (Exception ignore) {
                        //server = null;
                        //hints = null;
                       // hints1 = null;
                    }

                    while (close_socket) {
                        // cокет передачи данных
                        try {
                            System.out.println("conected? _logical: "+mimport);
                            //Socket client = server.accept(hints1);
                            serverSocket2.setSoTimeout(5000);
                            java.net.Socket client2 = serverSocket2.accept();

                            //System.out.println("Got a client :) ... Finally, someone saw me through all the cover!");
                            //System.out.println();
                            InputStream sin = client2.getInputStream();
                            OutputStream sout = client2.getOutputStream();
                            client2.setReceiveBufferSize(size_of_system_buffer);
                            client2.setTcpNoDelay(true);
                            // read message and send it back
                            //String message = new BufferedReader(new InputStreamReader(client.getInputStream())).readLine();

                            byte hand_shake_buffer[] = new byte[2];
                            client2.getInputStream().read(hand_shake_buffer);
                            System.out.println("hand_shake_buffer[0] : " + mimport + " " + hand_shake_buffer[0]);
                            if (hand_shake_buffer[0] == 15) {
                                hand_shake_buffer[0] = 20;
                                //send 20 all right
                                client2.getOutputStream().write(hand_shake_buffer);
                                who_rec = mimport;

                                //System.out.println("buffer");
                                ByteBuffer buffer2 = ByteBuffer.allocate(2);
                                //System.out.println("buffer2");
                                data_rcv.add(new short[samples * 1]);
                                System.out.println("data_rcv");


                                sync_i = 4001;
                                /*
                                for (local_i = 1; local_i <= (225); local_i++) {
                                    client.getInputStream().read(buffer);
                                    //System.out.println("i "+local_i);
                                    //buffer2.order(ByteOrder.LITTLE_ENDIAN);
                                    for (int j = 1; j <= 98; j++) {

                                        buffer2.put(buffer[j * 2 - 2]);
                                        buffer2.put(buffer[j * 2 - 1]);
                                        // System.out.println("buffer2 in "+buffer2);
                                        data_rcv.get(data_rcv.size - 1)[sync_i] = buffer2.getShort(0);
                                        buffer2.clear();
                                        sync_i++;
                                    }
                                    //buffer2.putShort(data.get(0)[i]);

                                }*/
                                info_i=1;
                                byte null_bute=0;
                                boolean get_wrong_block=true;
                                //int sended_blocks=0;

                                //client2.getInputStream().read(system_buffer);
                               // system_buffer[0]=20;
                                //Thread.sleep(1000);
                                //client2.getOutputStream().write(system_buffer[0]);
                                //sended_blocks++;
                                //client.getOutputStream().flush();

                                //System.out.println("Recieve: "+sended_blocks+" b1: "+system_buffer[0]+" b2: "+system_buffer[1]);
                                sync_i=9000;
                                for (int i=0;i<samples;i++){
                                   if (sync_i>=size_of_system_buffer/2){
                                       //Arrays.fill(system_buffer,null_bute);
                                       System.out.println("System read");
                                       client2.getInputStream().read(system_buffer);
                                       system_buffer[0]=20;
                                       //Thread.sleep(1000);
                                       client2.getOutputStream().write(system_buffer[0]);
                                       sync_i=0;
                                    info_i++;
                                   }
                                   buffer2.put(system_buffer[sync_i*2+0]);
                                   buffer2.put(system_buffer[sync_i*2+1]);
                                    //System.out.println("N: "+system_buffer[i*2+0]);

                                   data_rcv.get(data_rcv.size - 1)[i] = buffer2.getShort(0);

                                    //System.out.println("N: "+i+" s: "+data_rcv.get(data_rcv.size - 1)[i]);

                                   buffer2.clear();
                                   sync_i++;


                                }

                                System.out.println("ublocked");
                                blocked.add(false);
                                System.out.println("NEXT");
                            } else {

                                //обратная пересылка ------------------------------------------------------------------------------------------------->
                                if (blocked != null) {
                                    if ((blocked.size > 0)&((current_block.get(number_block))<blocked.size)) {
                                        if (!blocked.get(current_block.get(number_block))) {
                                            if (who_rec != mimport) {
                                                if (current_block.get(number_block)==0) {
                                                    System.out.println("Start send");


                                                    hand_shake_buffer[0] = 25;
                                                    client2.getOutputStream().write(hand_shake_buffer);
                                                    ByteBuffer buffer2 = ByteBuffer.allocate(2);
                                                    sync_j = 0;
                                                    for (int i = 1; i <= (225); i++) {
                                                        for (int j = 1; j <= (98); j++) {
                                                            buffer2.putShort(data_rcv.get(0)[sync_j]);
                                                            buffer[j * 2 - 2] = buffer2.get(0);
                                                            buffer[j * 2 - 1] = buffer2.get(1);
                                                            buffer2.clear();
                                                            sync_j++;
                                                        }
                                                        client2.getOutputStream().write(buffer);

                                                    }

                                                    current_block.set(number_block, (short) (current_block.get(number_block)+1));
                                                    //data_rcv.removeIndex(0);
                                                    //data_rcv.clear();

                                                    //blocked.removeIndex(0);
                                                }else
                                                {
                                                    hand_shake_buffer[0] = 20;
                                                    client2.getOutputStream().write(hand_shake_buffer);
                                                }

                                            } else {
                                                hand_shake_buffer[0] = 20;
                                                client2.getOutputStream().write(hand_shake_buffer);
                                            }

                                        } else {
                                            hand_shake_buffer[0] = 20;
                                            client2.getOutputStream().write(hand_shake_buffer);
                                        }
                                    } else {
                                        hand_shake_buffer[0] = 20;
                                        client2.getOutputStream().write(hand_shake_buffer);
                                    }
                                } else {
                                    hand_shake_buffer[0] = 20;
                                    client2.getOutputStream().write(hand_shake_buffer);
                                }

                                //----------------------------------------------------------------------------------------------------------------------------------->


                            }

                            //buffer[0]=125;


                            //Gdx.app.log("PingPongSocketExample", "got client message: "+buffer[0]+" "+buffer[1]+" "+buffer[2]+" "+buffer[3]+" ");
                            //System.out.println("got client message: " + buffer);
                            //client.getOutputStream().write("PONG\n".getBytes());
                        } catch (Exception e) {

                            if (dynamic_port>9000){

                                close_socket=false;
                                clients_online--;
                            }
                            Gdx.app.log("PingPongSocketExample", "an error occured", e);
                        }

                    }
                }
            }
        }).start();

    }

    public void play_snd(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                int j=0;
                while (true) {
                    try {
                        if (blocked != null) {
                            if (blocked.size > 0) {
                                if (!blocked.get(0)) {
                                    blocked.set(0,false);
                                    //System.out.println("Play : Start");
                                    player.writeSamples(data_rcv.get(0), 0, 22050);
                                    //System.out.println("Play : End "+data_rcv.get(0).length);
                                    // System.out.println("buffer.rcv 5000 "+data_rcv.get(0)[5000]);
                                    //System.out.println("buffer. 5000 "+data.get(0)[5000]);
                                    //System.out.println("buffer.rcv 15000 "+data_rcv.get(0)[15000]);
                                    //System.out.println("buffer. 15000 "+data.get(0)[15000]);
                                    // System.out.println("buffer.rcv 25000 "+data_rcv.get(0)[25000]);
                                    // System.out.println("buffer. 25000 "+data.get(0)[25000]);
                                    // System.out.println("buffer.rcv 35000 "+data_rcv.get(0)[35000]);
                                    // System.out.println("buffer. 35000 "+data.get(0)[35000]);
                                    //System.out.println("buffer.rcv 43000 "+data_rcv.get(0)[43000]);
                                    // System.out.println("buffer. 43000 "+data.get(0)[43000]);

                                    data_rcv.removeIndex(0);
                                    //data_rcv.clear();

                                    blocked.removeIndex(0);


                                    //player.writeSamples(data.get(0), 0, data.get(0).length);
                                } else {
                                    System.out.println("Data blocked");
                                }
                                //player.writeSamples(data, 0, data.length);
                            }

                        }
                    }catch (Exception e){
                        Gdx.app.log("PingPongSocketExample", "an error occured", e);
                    }
                    System.out.print("");
                }

            }
        }).start();

    }
    public void save_snd(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (touched) {
                        //player.setVolume(0.0f);
                        touched = false;
                        //recorder = Gdx.audio.newAudioRecorder(samples, isMono);
                        data.add(new short[samples * 1]);
                        blocked.add(true);
                        //System.out.println("Record: Start");
                        recorder.read(data.get(data.size - 1), 0, data.get(data.size - 1).length);
                        //recorder.read(data, 0, data.length);
                        //System.out.println("Record: End");
                        blocked.set(blocked.size - 1, false);
                        // player.setVolume(1.0f);


                        //player.dispose();
                        send_msg();
                    }
                }
            }
        }).start();
    }
}
