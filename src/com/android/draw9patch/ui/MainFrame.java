/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.draw9patch.ui;

import com.android.draw9patch.ui.action.ExitAction;
import com.android.draw9patch.ui.action.OpenAction;
import com.android.draw9patch.ui.action.SaveAction;
import com.android.draw9patch.ui.action.SaveAsAction;
import com.android.draw9patch.graphics.GraphicsUtilities;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.ActionMap;
import javax.swing.JFileChooser;
import javax.imageio.ImageIO;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ExecutionException;

import org.jdesktop.swingworker.SwingWorker;

public class MainFrame extends JFrame
{
	private ActionMap actionsMap;
	private JMenuItem saveAsMenuItem;
	private JMenuItem saveMenuItem;
	private ImageEditorPanel imageEditor;

	private static final String TITLE_FORMAT = "Draw9PatchExtended: %s";

	public MainFrame(String path) throws HeadlessException
	{
		super("Draw9PatchExtended");

		buildActions();
		buildMenuBar();
		buildContent();

		if (path == null)
		{
			showOpenFilePanel();
		} else
		{
			try
			{
				File file = new File(path);
				BufferedImage img = GraphicsUtilities.loadCompatibleImage(file.toURI().toURL());
				showImageEditor(img, file.getAbsolutePath());

				setTitle(String.format(TITLE_FORMAT, path));
			} catch (Exception ex)
			{
				showOpenFilePanel();
			}
		}

		// pack();
		setSize(1024, 600);
	}

	private void buildActions()
	{
		actionsMap = new ActionMap();
		actionsMap.put(OpenAction.ACTION_NAME, new OpenAction(this));
		actionsMap.put(SaveAsAction.ACTION_NAME, new SaveAsAction(this));
		actionsMap.put(SaveAction.ACTION_NAME, new SaveAction(this));
		actionsMap.put(ExitAction.ACTION_NAME, new ExitAction(this));
	}

	private void buildMenuBar()
	{
		JMenu fileMenu = new JMenu("File");
		JMenuItem openMenuItem = new JMenuItem();
		saveAsMenuItem = new JMenuItem();
		saveMenuItem = new JMenuItem();
		JMenuItem exitMenuItem = new JMenuItem();

		openMenuItem.setAction(actionsMap.get(OpenAction.ACTION_NAME));
		fileMenu.add(openMenuItem);

		saveMenuItem.setAction(actionsMap.get(SaveAction.ACTION_NAME));
		saveMenuItem.setEnabled(false);
		fileMenu.add(saveMenuItem);

		saveAsMenuItem.setAction(actionsMap.get(SaveAsAction.ACTION_NAME));
		saveAsMenuItem.setEnabled(false);
		fileMenu.add(saveAsMenuItem);

		exitMenuItem.setAction(actionsMap.get(ExitAction.ACTION_NAME));
		fileMenu.add(exitMenuItem);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		setJMenuBar(menuBar);
	}

	private void buildContent()
	{
		setContentPane(new GradientPanel());
	}

	private void showOpenFilePanel()
	{
		add(new OpenFilePanel(this));
	}

	public static File lastOpenFile;

	public SwingWorker<?, ?> open(File file)
	{
		if (file == null)
		{
			JFileChooser chooser;
			if (lastOpenFile != null)
			{
				chooser = new JFileChooser(lastOpenFile.getParentFile());
			} else
			{
				chooser = new JFileChooser();
			}

			chooser.setFileFilter(new PngFileFilter());
			int choice = chooser.showOpenDialog(this);
			if (choice == JFileChooser.APPROVE_OPTION)
			{
				File selectedFile = chooser.getSelectedFile();
				lastOpenFile = selectedFile;
				return new OpenTask(selectedFile);
			} else
			{
				return null;
			}
		} else
		{
			return new OpenTask(file);
		}
	}

	void showImageEditor(BufferedImage image, String name)
	{
		getContentPane().removeAll();
		imageEditor = new ImageEditorPanel(this, image, name);
		add(imageEditor);
		saveAsMenuItem.setEnabled(true);
		saveMenuItem.setEnabled(true);
		validate();
		repaint();

		imageEditor.invalidateView();
	}

	public SwingWorker<?, ?> save()
	{
		if (imageEditor == null)
		{
			return null;
		}

		File file = imageEditor.chooseLastSaveFile();
		return new SaveTask(file);
	}
	
	public SwingWorker<?, ?> saveAs()
	{
		if (imageEditor == null)
		{
			return null;
		}

		File file = imageEditor.chooseSaveFile();
		return file != null ? new SaveTask(file) : null;
	}

	private class SaveTask extends SwingWorker<Boolean, Void>
	{
		private final File file;

		SaveTask(File file)
		{
			this.file = file;
		}

		protected Boolean doInBackground() throws Exception
		{
			try
			{
				ImageIO.write(imageEditor.getImage(), "PNG", file);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			return true;
		}
	}

	private class OpenTask extends SwingWorker<BufferedImage, Void>
	{
		private final File file;

		OpenTask(File file)
		{
			this.file = file;
		}

		protected BufferedImage doInBackground() throws Exception
		{
			return GraphicsUtilities.loadCompatibleImage(file.toURI().toURL());
		}

		@Override
		protected void done()
		{
			try
			{
				showImageEditor(get(), file.getAbsolutePath());
				setTitle(String.format(TITLE_FORMAT, file.getAbsolutePath()));
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			} catch (ExecutionException e)
			{
				e.printStackTrace();
			}
		}
	}
}
