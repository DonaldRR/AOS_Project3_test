import java.io.*;
import java.util.*;

/////////test for github

public class main {
    public static void main(String[] args) {


        Vector quorum = new Vector();
        quorum.add(0 , "1 2 4");
        quorum.add(1 , "1 2 5");
        quorum.add(2 , "1 3 6");
        quorum.add(3 , "1 3 7");
        quorum.add(4 , "2 4 3 6");
        quorum.add(5 , "2 4 3 7");
        quorum.add(6 , "2 5 3 6");
        quorum.add(7 , "2 5 3 7");
        quorum.add(8 , "1 4 5");
        quorum.add(9 , "1 6 7");
        quorum.add(10 , "4 5 3 6");
        quorum.add(11 , "4 5 3 7");
        quorum.add(12 , "2 4 6 7");
        quorum.add(13 , "2 5 6 7");
        quorum.add(14 , "4 5 6 7");


        boolean cnt = true;

        System.out.println("Node Number?");
        Scanner cn = new Scanner(System.in).useDelimiter("\\s");
        //Node node = new Node(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        Node node = new Node(cn.nextInt());
        //node.connection();
/*
        System.out.println("Would you like apply mutual algorithm?");
        Scanner cn_m = new Scanner(System.in).useDelimiter("\\s");
        if(cn_m.next().equals("y")) node.setMutual_exclusion(true);
        else node.setMutual_exclusion(false);
*/
        node.setMutual_exclusion(false);
        try {

            Thread server = new Thread(new com_server(node));

            server.start();


            while (cnt && node.getNid() <8 ) {

                try {


                    if(node.getNid()!=0) {

                        System.out.print("Node of " + node.getNid() + ", number of message sent: " + node.getNm_msg_sent() + "\t");
                        System.out.print("Node of " + node.getNid() + ", number of message received: " + node.getNm_msg_received() + "\r");

                    }

                    }catch(Exception e){
                        e.printStackTrace();
                    }

                }


                while (cnt && node.getNid() >= 8) {
                    try {

                        System.out.println("Would you like to make request? (y/n)");
                        Scanner cmd = new Scanner(System.in).useDelimiter("\\s");


                        if (cmd.next().equals("y")) {
                            int iteration = 0;
                            node.setIteration(iteration);
                            int target_set;
                            String set[];
                            Hashtable<String, Boolean> quorum_set ;
                            node.resetNm_msg_sent();
                            node.resetsetNm_msg_received();


                            while(node.getIteration()<20 ) {
                                long start= System.nanoTime();
                                int temp_msg_nm = node.getNm_msg_sent()+node.getNm_msg_received();
                                node.setRequest_act_finished(false);
                                target_set = (int) (Math.random() * 100) % 15;
                                set = quorum.get(target_set).toString().split(" ");
                                quorum_set = new Hashtable();
                                for (String t_in : set)
                                    quorum_set.put(t_in, false);

                                node.setAimed_quorum(iteration, quorum_set);

                                node.setLogical_time_unit_increase();
                                request_message req_msg = new request_message(node.getNid(), -1, node.getLogical_time(), iteration,
                                        request_message.action_options.request, request_message.calling_option.broadcast_clique);

                                com_requester req_begin = new com_requester(req_msg, quorum_set, node);

                                req_begin.send();


                                checker(node, iteration);

                                request_message cs_msg = new request_message(node.getNid(), -1, node.getLogical_time(), iteration,
                                        request_message.action_options.enter_cs, request_message.calling_option.broadcast_clique);
                                com_requester cs_inform = new com_requester(cs_msg, quorum_set, node);
                                cs_inform.send();

                                critical_section(node, start, temp_msg_nm);

                                release_resource(node, iteration, quorum_set);




                                iteration++;
                                node.setIteration(iteration);
                                Thread.sleep(5);
                            }

                        node.getWhole().clear();

                        request_message complete_msg = new request_message(node.getNid(), 0, node.getLogical_time(), iteration,
                                request_message.action_options.complete, request_message.calling_option.single);

                        com_requester cp_bk = new com_requester(complete_msg, node);
                        cp_bk.send();

                        System.out.println("Node of " + node.getNid()+ ", number of message sent: " + node.getNm_msg_sent());
                        System.out.println("Node of " + node.getNid()+ ", number of message received: " + node.getNm_msg_received());

                        //System.out.println("Size of Buffer: " + node.getBuffer().size());


                    }
                    else {
                        System.out.println("Goodbye!!!");
                        cnt = false;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }


        }catch (Exception e) {e.printStackTrace();}


    }

    public static void checker(Node node, int iteration) throws Exception{
        int temp;
        while(!node.getChecker()) {
            //System.out.print("Iteration: "+ iteration + " Updated Quorum: " + node.getAimed_quorum(iteration)+"\r");

            Enumeration qs_checker = node.getAimed_quorum(iteration).elements();
            temp=0;
            while(qs_checker.hasMoreElements()){
                if(qs_checker.nextElement().equals(true)){
                    temp++;
                }
            }

            if (temp == node.getAimed_quorum(iteration).size()) {
                System.out.println("Receive permission for critical sections.");
                System.out.println("Iteration: " + iteration +
                        " Requirement no. permission: " + node.getAimed_quorum(iteration).size());
                System.out.println("Quorum set in CS: " + node.getAimed_quorum(iteration));
                node.setChecker(true);
            }

        }

        if(node.getChecker()) {
            node.setCs_permission(true);
        }
    }

    public static void critical_section(Node node, long start, int temp_msg_nm) throws Exception{
        while (node.getCs_permission()){
            node.setChecker(false);
            if( !node.getRequest_act_finished() && node.getNid() < 6){
                //System.out.println("Enter Critical Section!!!!!!!!!!!");
                node.setCs_permission(false);
            }

            else if ( !node.getRequest_act_finished() && node.getNid() >= 6) {
                long elapsed_time = System.nanoTime()-start;
                int total_msg_nm = node.getNm_msg_sent()+node.getNm_msg_received()-temp_msg_nm;
                //System.out.println("In critical section of Critical Section.");
                //write_file(node, elapsed_time,total_msg_nm);
                node.setCs_permission(false);


            }
        }
    }

    public static void release_resource(Node node,int iteration, Hashtable for_release){

            //System.out.println("Releasing Def.");
            request_message release_msg = new request_message(node.getNid(),-1,node.getLogical_time(), iteration,
                    request_message.action_options.release, request_message.calling_option.broadcast_clique);
            com_requester release = new com_requester(release_msg, for_release, node);
            release.send();

    }



    public static void write_file(Node node, long elpased_time, int total_msg_nm) throws Exception{
        //System.out.println("Enter write file.");
        //String home = System.getProperty("c:\\Users\\a62ba\\Desktop\\Spring_2019_CS6378_AOS_Undergoing");
        String cwd = System.getProperty("user.dir") + "\\record.txt";
        //String cwd = System.getProperty("user.home\\server01") + "\\record.txt";
        //String cwd = System.getProperty("\\home\\eng\\b\\bxw170030") + "\\record.txt";
        //String cwd = System.getProperty("user.home") + "\\record.txt";
        File f = new File(cwd);
        if (!f.exists()) { f.createNewFile(); }

        //String in = "entering node " + this.node.getNid() + " \t" + "Time Stamp " + this.node.getLogical_time()+ "\n";
        //String in = "Test_Read_8:  " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        //String in = "< Client ID:  " + this.node.getNid() + ", " + " Timestamp: " + this.node.getLogical_time() + " >";
        //String in = "< Client:  " + ori_msg.getReq_initiator()+ ", " + " Timestamp: " + ori_msg.getReq_logical_time() + " >";


        String in = "Iteration: " + node.getIteration()
                    +" entering\n < Client:  " + node.getNid()+ ", " + "Physical time: " + System.currentTimeMillis() + " >\n"
                    +" elapsed time: " + elpased_time + ", total number of message: " + total_msg_nm;




        String serial_no=readFromLast(node);
        String old_no = "";
        String new_no = "";

        /*
        if(serial_no.equals(""))
        //if(f.length() == 1)
            serial_no = "--- 1 ---";

        else {
            old_no = serial_no.substring(serial_no.indexOf(" ")+1, serial_no.indexOf(" ",(serial_no.indexOf(" ")+1)));
            new_no = Integer.toString(Integer.parseInt(old_no) + 1);
            serial_no = "--- " + new_no + " ---";
        }
        String in = serial_no + "Iteration: " + node.getIteration()
                +" entering < Client:  " + node.getNid()+ ", " + "Physical time: " + System.currentTimeMillis() + " > "
                +" elapsed time: " + elpased_time + ", total number of message: " + total_msg_nm;

*/
        PrintWriter out = new PrintWriter(new FileWriter(f, true));
        out.println(in);

        Thread.sleep(3);
        out.close();

        System.out.println("Exit file write.");


    }


    public static String readFromLast( Node node ) throws Exception{
        String temp="";
        //File file = new File(System.getProperty(System.getProperty("user.dir"))+ "/record.txt");
        //File file = new File(System.getProperty("C:\\Users\\a62ba\\Desktop\\Spring_2019_CS6378_AOS_Undergoing\\AOS_Project1_ori\\record.txt"));
        //System.out.println("Enter Read file.");
        String cwd = System.getProperty("user.dir") + "\\record.txt";
        //String cwd = System.getProperty("\\home\\eng\\b\\bxw170030\\sever01") + "\\record.txt";
        //String cwd = System.getProperty("user.home") + "record.txt";
        File file = new File(cwd);
        if (!file.exists()) {file.createNewFile();}
        //int lines = 0;
        StringBuilder builder = new StringBuilder();
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
            long fileLength = file.length() - 1;
            if(fileLength < 0) fileLength =0;
            // Set the pointer at the last of the file
            randomAccessFile.seek(fileLength);
            for(long pointer = fileLength; pointer >= 0; pointer--){
                randomAccessFile.seek(pointer);
                char c;
                // read from the last one char at the time
                c = (char)randomAccessFile.read();
                // break when end of the line
                if(c == '\n' && pointer != fileLength){
                    break;
                }
                builder.append(c);
            }
            // Since line is read from the last so it
            // is in reverse so use reverse method to make it right
            builder.reverse();
            temp = builder.toString();
            System.out.println("Result of request: " + temp);
            //node.setRead_string(temp);
            //node.getReceived_msg().setRequest_result(temp);
            //node.setCarrier_msg_w_Stg(node.getCarrier_msg(), temp);
            //node.getCarrier_msg().setAction(message.msg_options.task_finished);
            //System.out.println("Line - " + temp);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            if(randomAccessFile != null){
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return temp;
    }

    public static String partition_selected(Node node, int iteration){
        String target= "";
        switch(iteration){
            case 0:
                target = "A B C D E F G H";
                break;
            case 1:
                if(node.getNid().compareTo("D") <= 0)
                   target = "A B C D";
                else
                    target = "E F G H";
                break;
            case 2:
                if(node.getNid().equals("A") || node.getNid().equals("H"))
                    target = "";
                else if(node.getNid().compareTo("B")>=0 && node.getNid().compareTo("D")<=0)
                    target = "B C D";
                else if(node.getNid().compareTo("E")>=0 && node.getNid().compareTo("G")<=0)
                    target = "E F G";
                break;
            case 3:
                if(node.getNid().equals("A") || node.getNid().equals("H"))
                    target = "";
                else if(node.getNid().compareTo("B")>=0 && node.getNid().compareTo("G")<=0)
                target = "B C D E F G";
        }

        return target;
    }


}

