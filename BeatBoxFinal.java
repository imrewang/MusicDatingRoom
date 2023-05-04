package AppendixA;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.io.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;
import java.net.*;


public class BeatBoxFinal {  // implements MetaEventListener//节拍器

    JPanel mainPanel;
    JList incomingList;
    JTextField userMessage;
    ArrayList<JCheckBox> checkboxList;
    int nextNum;//发送消息次数
    ObjectInputStream in;
    ObjectOutputStream out;
    Vector<String> listVector = new Vector<String>();
    String userName;
    HashMap<String, boolean[]> otherSeqsMap = new HashMap<String, boolean[]>();
    Sequencer sequencer;
    Sequence sequence;
    Sequence mySequence = null;
    Track track;
    JFrame theFrame;

    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat",
            "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap",
            "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga",
            "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo",
            "Open Hi Conga"};//乐器名称
    int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};


    public static void main(String[] args) {
        //new BeatBoxFinal().startUp(args[0]);
        Scanner s = new Scanner(System.in);
        System.out.print("Please input your name : ");
        String name = s.nextLine();

        new BeatBoxFinal().startUp(name);
    }

    public void startUp(String name) {
        userName = name;
        try {
            Socket sock = new Socket("127.0.0.1", 4242);
            out = new ObjectOutputStream(sock.getOutputStream());
            in = new ObjectInputStream(sock.getInputStream());
            Thread remote = new Thread(new RemoteReader());
            remote.start();
        } catch (Exception ex) {
            System.out.println("couldn't connect - you'll have to play alone.");
        }
        setUpMidi();
        buildGUI();
    }

    public void buildGUI() {
        theFrame = new JFrame("Cyber BeatBox");//使用指定的标题创建一个新的，最初不可见的Frame 。
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//设置当用户在此帧上启动“关闭”时默认发生的操作。
        BorderLayout layout = new BorderLayout();//构造一个新的边框布局，组件之间没有间隙。
        JPanel background = new JPanel(layout);//使用指定的布局管理器创建新的缓冲JPanel
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        //出厂标准Border对象的工厂类。 //创建一个占用空间但没有绘图的空边框，指定顶部，左侧，底部和右侧的宽度。
        //设置此组件的边框。

        checkboxList = new ArrayList<JCheckBox>();//复选框的实现 - 可以选择或取消选择的项目，并向用户显示其状态。
        Box buttonBox = new Box(BoxLayout.Y_AXIS);
        //Box一个轻量级容器，它使用BoxLayout对象作为其布局管理器。
        //BoxLayout布局管理器，允许垂直或水平布置多个组件。
        //Y_AXIS指定组件应从上到下排列。

        //下面右列按钮
        JButton start = new JButton("Start");//创建一个带文本的按钮。
        start.addActionListener(new MyStartListener());//添加指定的动作侦听器以从此按钮接收动作事件。
        buttonBox.add(start);//将指定的组件追加到此容器的末尾。


        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo Up");//节奏加快
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);

        JButton sendIt = new JButton("sendIt");
        sendIt.addActionListener(new MySendListener());
        buttonBox.add(sendIt);


        JButton saveIt = new JButton("Serialize It");  // new button
        saveIt.addActionListener(new MySendListener());
        buttonBox.add(saveIt);
        //上面都是右列的按钮

        //下面是右列中间的输入框
        userMessage = new JTextField();//JTextField是一个轻量级组件，允许编辑单行文本。
        buttonBox.add(userMessage);//将指定的组件追加到此容器的末尾。

        //右列下方的显示框
        incomingList = new JList();
        incomingList.addListSelectionListener(new MyListSelectionListener());
        //向列表添加侦听器，以便在每次更改选择时收到通知; 听取选择状态变化的首选方式。
        incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);//设置列表的选择模式。
        //一次只能选择一个列表索引。
        JScrollPane theList = new JScrollPane(incomingList);//提供轻量级组件的可滚动视图。
        buttonBox.add(theList);
        incomingList.setListData(listVector);//从项目数组构造只读ListModel ，并使用此模型调用setModel 。
        //listVector的类型Vector<String>

        //左侧复选框
        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < 16; i++) {
            nameBox.add(new Label(instrumentNames[i]));
            //Label对象是用于在容器中放置文本的组件。 标签显示一行只读文本。 应用程序可以更改文本，但用户无法直接编辑它。
        }

        background.add(BorderLayout.EAST, buttonBox);//东部布局约束（容器的右侧）。
        background.add(BorderLayout.WEST, nameBox);//西部布局约束（容器的左侧）。

        theFrame.getContentPane().add(background);//返回此帧的 contentPane对象。

        GridLayout grid = new GridLayout(16, 16);//创建具有指定行数和列数的网格布局。
        grid.setVgap(1);//获取组件之间的垂直间隙。
        grid.setHgap(2);//获取组件之间的水平间隙。
        mainPanel = new JPanel(grid);//JPanel是一个通用的轻量级容器。
        background.add(BorderLayout.CENTER, mainPanel);


        for (int i = 0; i < 256; i++) {
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkboxList.add(c);
            mainPanel.add(c);
        } // end loop

        theFrame.setBounds(50, 50, 300, 300);//移动并调整此组件的大小。 左上角的新位置由x和y指定，新大小由width和height指定。
        theFrame.pack();//使此窗口的大小适合其子组件的首选大小和布局。
        theFrame.setVisible(true);
    } // close method


    public void setUpMidi() {
        try {
            sequencer = MidiSystem.getSequencer();//获得连接到默认设备的默认Sequencer 。
            sequencer.open();
            // sequencer.addMetaEventListener(this);
            sequence = new Sequence(Sequence.PPQ, 4);//构造具有指定时序分割类型和时序分辨率的新MIDI序列。
            //Sequence.PPQ是Java音乐API中的一个常量，表示每分钟的MIDI定时器滴答数。PPQ代表Pulses Per Quarter Note（每个四分音符的脉冲数），也称为“时基”。
            //乐器数字interface(MIDI)标准定义了电子音乐设备(例如电子键盘乐器和个人计算机)的通信协议。
            track = sequence.createTrack();//作为此序列的一部分，创建一个新的，最初为空的轨道。
            sequencer.setTempoInBPM(120);//以每分钟节拍数设置速度。 播放的实际速度是指定值和速度因子的乘积。

        } catch (Exception e) {
            e.printStackTrace();//将此throwable及其回溯打印到标准错误流。
        }
    } // close method

    public void buildTrackAndStart() {
        // this will hold the instruments for each vertical column,
        // in other words, each tick (may have multiple instruments)
        //这将保存每个垂直列的工具，换句话说，每个报价单（可能有多个工具）
        ArrayList<Integer> trackList = null;

        sequence.deleteTrack(track);//从序列中删除指定的轨道。
        track = sequence.createTrack();


        for (int i = 0; i < 16; i++) {
            trackList = new ArrayList<Integer>();
            for (int j = 0; j < 16; j++) {//横竖都各有16个格格
                JCheckBox jc = (JCheckBox) checkboxList.get(j + (16 * i));
                //复选框的实现 - 可以选择或取消选择的项目，并向用户显示其状态。
                if (jc.isSelected()) {//返回按钮的状态。 如果选择了切换按钮，则为True;如果不是，则为false。
                    int key = instruments[i];// {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};
                    trackList.add(key);
                } else {
                    trackList.add(null);
                }
            } // close inner

            makeTracks(trackList);
        } // close outer

        track.add(makeEvent(192, 9, 1, 0, 15)); // - so we always go to full 16 beats
        //所以我们总是达到完整的 16 拍

        try {

            sequencer.setSequence(sequence);//设置顺控程序运行的当前顺序。
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);//获得播放的重复次数。
            //LOOP_CONTINUOUSLY 一个值，指示循环应该无限期地继续而不是在特定数量的循环之后完成。
            sequencer.start();//开始播放当前加载的序列中的MIDI数据。
            sequencer.setTempoInBPM(120);//以每分钟节拍数设置速度。
        } catch (Exception e) {
            e.printStackTrace();
        }

    } // close method

//============================================================== inner class listeners

    public class MyStartListener implements ActionListener {
        //ActionListener用于接收动作事件的侦听器接口。 对处理动作事件感兴趣的类实现此接口，并使用组件的addActionListener方法向组件注册使用该类创建的对象。
        // 当动作事件发生时，将调用该对象的actionPerformed方法。
        public void actionPerformed(ActionEvent a) {
            //ActionEvent一个语义事件，指示发生了组件定义的操作。
            buildTrackAndStart();
        }
    }

    public class MyStopListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            sequencer.stop();//停止录制（如果有效），并播放当前加载的序列（如果有）。
        }
    }

    public class MyUpTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            float tempoFactor = sequencer.getTempoFactor();//返回音序器的当前速度因子。
            sequencer.setTempoFactor((float) (tempoFactor * 1.03));//按提供的系数缩放音序器的实际播放速度。
        }
    }

    public class MyDownTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * .97));
        }
    }

    public class MySendListener implements ActionListener {    // new - save
        public void actionPerformed(ActionEvent a) {
            // make an arraylist of just the STATE of the checkboxes
            //制作一个仅包含复选框状态的数组列表

            boolean[] checkboxState = new boolean[256];//16*16

            for (int i = 0; i < 256; i++) {
                JCheckBox check = (JCheckBox) checkboxList.get(i);
                if (check.isSelected()) {
                    checkboxState[i] = true;
                }
            }

            try {
                out.writeObject(userName + nextNum++ + ": " + userMessage.getText());
                out.writeObject(checkboxState);//将指定的对象写入ObjectOutputStream。
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("sorry dude. Could not send it to the server");
            }

        } // close method
    } // close inner class

    public class MyListSelectionListener implements ListSelectionListener {//列表选择值更改时通知的侦听器。

        public void valueChanged(ListSelectionEvent le) {//只要选择的值发生变化，就会调用。
            //ListSelectionEvent表征选择变化的事件。
            if (!le.getValueIsAdjusting()) {
                //返回这是否是一系列多个事件中的一个，其中仍在进行更改。
                String selected = (String) incomingList.getSelectedValue();
                //返回最小的选定单元格索引的值; 在列表中仅选择单个项目时所选的值 。
                if (selected != null) {
                    boolean[] selectedState = (boolean[]) otherSeqsMap.get(selected);//返回指定键映射到的值，如果此映射不包含键的映射，则返回 null 。
                    changeSequence(selectedState);
                    sequencer.stop();
                    buildTrackAndStart();
                }
            }
        }
    }

    public class RemoteReader implements Runnable {
        boolean[] checkboxState = null;
        String nameToShow = null;
        Object obj = null;//类Object是类层次结构的根。 每个class都有Object作为超类。 所有对象（包括数组）都实现此类的方法。

        public void run() {
            try {
                while ((obj = in.readObject()) != null) {
                    System.out.println("got an object from server");
                    System.out.println(obj.getClass());//返回此Object的运行时类。 返回的类对象是由所表示的类的static synchronized方法锁定的对象。
                    String nameToShow = (String) obj;
                    checkboxState = (boolean[]) in.readObject();//发来的节奏
                    //checkboxState是与nameToShow相关联的数据，用于存储与nameToShow相关的复选框状态。
                    //从ObjectInputStream中读取一个对象。 读取对象的类，类的签名，以及类的非瞬态和非静态字段及其所有超类型的值。
                    otherSeqsMap.put(nameToShow, checkboxState);//不同名字和不同num对应的音乐
                    listVector.add(nameToShow);//不同信息对应的名字
                    incomingList.setListData(listVector);//右下角不同人发的信息
                    //incomingList.setListData(listVector);//从项目数组构造只读ListModel ，并使用此模型调用setModel 。
                    //listVector的类型Vector<String>
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


//==============================================================

    public void changeSequence(boolean[] checkboxState) {
        for (int i = 0; i < 256; i++) {
            JCheckBox check = (JCheckBox) checkboxList.get(i);
            if (checkboxState[i]) {
                check.setSelected(true);
            } else {
                check.setSelected(false);
            }
        }
    }

    public void makeTracks(ArrayList<Integer> list) {
        Iterator it = list.iterator();
        for (int i = 0; i < 16; i++) {
            Integer num = (Integer) it.next();
            if (num != null) {
                int numKey = num.intValue();//返回此值 Integer为 int 。
                track.add(makeEvent(144, 9, numKey, 100, i));
                track.add(makeEvent(128, 9, numKey, 100, i + 1));
            }
        }
    }


    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two);//设置MIDI信息的数据。
            //设置需要一个或两个数据字节的MIDI消息的参数。 如果消息只占用一个数据字节，则忽略第二个数据字节; 如果消息不接受任何数据字节，则忽略两个数据字节。
            event = new MidiEvent(a, tick);//MIDI事件包含MIDI信息和以刻度表示的相应时间戳，可以表示存储在MIDI文件或Sequence对象中的MIDI事件信息。


        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }
}

