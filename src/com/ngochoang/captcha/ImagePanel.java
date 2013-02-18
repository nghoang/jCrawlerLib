package com.ngochoang.captcha;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class ImagePanel extends JPanel {

	private Image img;

	public ImagePanel() {
	}

	public void SetImage(Image img) {
		this.img = img;
		Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
		setSize(size);
		setLayout(null);
	}

	public void SetImage(String img) {
		try {
			Image photo;
			photo = ImageIO.read(new File(img));
			SetImage(photo);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ImagePanel(Image img) {
		SetImage(img);
	}

	public void paintComponent(Graphics g) {
		g.drawImage(this.img, 0, 0, null);
	}

	public void RefreshImage() {
		Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
		setSize(size);
	}

}
