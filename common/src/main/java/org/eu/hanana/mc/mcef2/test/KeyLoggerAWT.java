package org.eu.hanana.mc.mcef2.test;

import java.awt.*;
import java.awt.event.*;

public class KeyLoggerAWT {

    public static void main(String[] args) {
        // 创建窗口
        Frame frame = new Frame("AWT KeyLogger Test");
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());
        
        // 文本区域显示按键
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setFocusable(false);
        frame.add(textArea, BorderLayout.CENTER);
        
        // 关闭窗口时退出程序
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        // 添加按键监听器
        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                textArea.append("Typed: keyChar=" + e.getKeyChar() + "\n");
            }

            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println(e.toString());
                textArea.append("Pressed: keyCode=" + e.getKeyCode() +
                        ", keyChar=" + e.getKeyChar() +
                        ", modifiers=" + KeyEvent.getModifiersExText(e.getModifiersEx()) + "\n");
            }

            @Override
            public void keyReleased(KeyEvent e) {
                textArea.append("Released: keyCode=" + e.getKeyCode() +
                        ", keyChar=" + e.getKeyChar() +
                        ", modifiers=" + KeyEvent.getModifiersExText(e.getModifiersEx()) + "\n");
            }
        });

        frame.setVisible(true);

        // 让窗口获取焦点，保证可以接收按键
        frame.requestFocus();
    }
}
