package bot;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.skype.Skype;
import com.skype.SkypeException;
import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;

public class TeslaLaunch extends Shell {
	public static void main(final String args[]) throws Exception {
		if (!Skype.isRunning()) {
			System.err.println("Skype isn't running!");
			System.exit(0);
		}
		final Display display = Display.getDefault();
		final TeslaLaunch shell = new TeslaLaunch(display, SWT.SHELL_TRIM);
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public TeslaLaunch(Display display, int style) throws ConnectorException {
		super(display, style);
		createContents();
	}

	int processes = 0;

	private void createContents() throws ConnectorException {
		setText("TeslaBot Input");
		setSize(400, 300);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		final TeslaInput input = new TeslaInput();

		final Text fromSkype = new Text(this, SWT.V_SCROLL | SWT.MULTI | SWT.READ_ONLY | SWT.BORDER);

		fromSkype.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1));
		Connector.getInstance().setDebugOut(new PrintWriter(new Writer() {
			@Override
			public void write(char[] cbuf, int off, int len) throws IOException {
				final String appended = new String(cbuf, off, len);
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						fromSkype.append(appended);
						if (appended.length() != 2) {
							String msg = appended.substring(3);
							if (appended.startsWith("<-")) {
								if (processes < 10) {
									try {
										processes++;
										input.getInput(msg);
									} catch (ConnectorException e) {
										e.printStackTrace();
									} catch (SkypeException e) {
										e.printStackTrace();
									} catch (IOException e) {
										e.printStackTrace();
									}
									processes--;
								}else{
									System.out.println("Process overload");
								}
							}
						}
					}
				});
			}

			@Override
			public void flush() throws IOException {
				// Do nothing
			}

			@Override
			public void close() throws IOException {
				// Do nothing
			}
		}));
		Connector.getInstance().setDebug(true);

		final Text toSkype = new Text(this, SWT.BORDER);
		toSkype.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

		final Button send = new Button(this, SWT.NONE);
		send.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				final String command = toSkype.getText();
				new Thread() { // Use execute(String) without waiting
					@Override
					public void run() {
						try {
							Connector.getInstance().execute(command);
						} catch (ConnectorException e) {
							// Skip not Skype errors
						}
					}
				}.start();
			}
		});
		send.setText("&Send");
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
