package com.att.aro.ui.view.video;

import javax.media.Controller;
import javax.media.Player;
import javax.media.Time;
import javax.swing.SwingUtilities;

import com.att.aro.core.ILogger;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.view.diagnostictab.DiagnosticsTab;

public class VideoSyncThread implements Runnable {

	private ILogger logger = ContextAware.getAROConfigContext().getBean(
			ILogger.class);

	private double seconds;
	private double userPausedPos;
	private double prevSeconds;
	private double timeAdjustment;
	private Player videoPlayer;
	private DiagnosticsTab aroAdvancedTab;
	private double videoOffset;
	private double userClickPosition;

	public VideoSyncThread(Player videoPlayer, DiagnosticsTab aroAdvancedTab,
			double videoOffset, double userClickPosition) {
		this.videoPlayer = videoPlayer;
		this.aroAdvancedTab = aroAdvancedTab;
		this.videoOffset = videoOffset;
		this.userClickPosition = userClickPosition;
	}

	@Override
	public void run() {

		// Run this in a loop if the video is started
		int state;
		do {

			// Get information from video player in this synchronized block
			// in case video is cleared while running.
			Time currentVideoTime;
			synchronized (this) {
				if (videoPlayer != null) {
					currentVideoTime = videoPlayer.getMediaTime();
					state = videoPlayer.getState();
				} else {
					break;
				}
			}
			// Check to see if video time has changed
			if (currentVideoTime != null
					&& currentVideoTime.getSeconds() != seconds) {
				if (aroAdvancedTab != null) {
					seconds = currentVideoTime.getSeconds();

					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							// Sync external video and traces, in case of
							// native video
							// normal behavior is retained.
							if (!aroAdvancedTab.IsGraphPanelClicked()) {
								userPausedPos = videoOffset;
								if ((seconds >= userPausedPos)) {
									timeAdjustment = (seconds - userPausedPos);
									aroAdvancedTab
											.setTimeLineLinkedComponents(
													timeAdjustment, true);

									prevSeconds = seconds;
								}
							} else {
								aroAdvancedTab.setTimeLineLinkedComponents(
										seconds + videoOffset, true);

							}

							// In case of native video , fall back on the
							// native track.

							if (prevSeconds > 0.0) {
								if (aroAdvancedTab.IsGraphPanelClicked()) {
									if (seconds <= 0.0) {
										/*
										 * if user starts the video slider
										 * from the begining reset the blue
										 * line
										 */
										aroAdvancedTab
												.setTimeLineLinkedComponents(
														-1.0, true);
										aroAdvancedTab
												.setGraphPanelClicked(false);

									} else {
										if (userClickPosition < videoOffset) {
											aroAdvancedTab
													.setTimeLineLinkedComponents(
															seconds
																	- videoOffset,
															true);

										} else {
											aroAdvancedTab
													.setTimeLineLinkedComponents(
															seconds
																	- videoOffset,
															true);
										}
									}
								} else {
									/*
									 * aroAdvancedTab.
									 * setTimeLineLinkedComponents( (seconds
									 * - prevSeconds) - timeAdjustment,
									 * true);
									 */
									aroAdvancedTab
											.setTimeLineLinkedComponents(
													timeAdjustment, true); // above
																			// commented
																			// code
																			// sending
																			// negative
																			// value
																			// due
																			// to
																			// that
																			// cursor
																			// is
																			// not
																			// in
																			// sync
								}
							} else {
								if (seconds <= 0.0) {
									/*
									 * if user starts the video slider from
									 * the begining reset the blue line
									 */
									aroAdvancedTab
											.setTimeLineLinkedComponents(
													-1.0, true);
									aroAdvancedTab
											.setGraphPanelClicked(false);
								} else {
									aroAdvancedTab
											.setTimeLineLinkedComponents(
													seconds + videoOffset,
													true);
								}
							}

						}
					});

				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException exception) {
				logger.error("InterruptedException", exception);
			}

		} while (state == Controller.Started);
	}

}
