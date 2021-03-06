/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.learningpath.ui;

import static org.olat.core.gui.components.util.KeyValues.entry;

import java.util.Arrays;
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.config.CompletionType;
import org.olat.course.config.CourseConfig;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 27 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathNodeConfigController extends FormBasicController {	

	public static final String CONFIG_KEY_DURATION = "duration";
	public static final String CONFIG_KEY_OBLIGATION = "obligation";
	public static final String CONFIG_DEFAULT_OBLIGATION = AssessmentObligation.mandatory.name();
	public static final String CONFIG_KEY_START = "start.date";
	public static final String CONFIG_KEY_TRIGGER = "fully.assessed.trigger";
	public static final String CONFIG_VALUE_TRIGGER_NODE_VISITED = "nodeVisited";
	public static final String CONFIG_VALUE_TRIGGER_CONFIRMED = "confirmed";
	public static final String CONFIG_VALUE_TRIGGER_STATUS_DONE = "statusDone";
	public static final String CONFIG_VALUE_TRIGGER_STATUS_IN_REVIEW = "statusInReview";
	public static final String CONFIG_VALUE_TRIGGER_SCORE = "score";
	public static final String CONFIG_VALUE_TRIGGER_PASSED = "passed";
	public static final String CONFIG_DEFAULT_TRIGGER = CONFIG_VALUE_TRIGGER_CONFIRMED;
	public static final String CONFIG_KEY_SCORE_CUT_VALUE = "scoreCutValue";
	
	private TextElement durationEl;
	private SingleSelection obligationEl;
	private DateChooser startDateEl;
	private SingleSelection triggerEl;
	private TextElement scoreCutEl;

	private final CourseConfig courseConfig;
	private final ModuleConfiguration moduleConfigs;
	private final LearningPathControllerConfig ctrlConfig;

	public LearningPathNodeConfigController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			ModuleConfiguration moduleConfig, LearningPathControllerConfig ctrlConfig) {
		super(ureq, wControl);
		this.courseConfig = CourseFactory.loadCourse(courseEntry).getCourseConfig();
		this.moduleConfigs = moduleConfig;
		this.ctrlConfig = ctrlConfig;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String estimatedTime = moduleConfigs.getStringValue(CONFIG_KEY_DURATION);
		durationEl = uifactory.addTextElement("config.duration", 128, estimatedTime , formLayout);
		
		KeyValues obligationKV = new KeyValues();
		obligationKV.add(entry(AssessmentObligation.mandatory.name(), translate("config.obligation.mandatory")));
		obligationKV.add(entry(AssessmentObligation.optional.name(), translate("config.obligation.optional")));
		obligationEl = uifactory.addRadiosHorizontal("config.obligation", formLayout, obligationKV.keys(), obligationKV.values());
		obligationEl.addActionListener(FormEvent.ONCHANGE);
		String obligationKey = moduleConfigs.getStringValue(CONFIG_KEY_OBLIGATION, CONFIG_DEFAULT_OBLIGATION);
		if (Arrays.asList(obligationEl.getKeys()).contains(obligationKey)) {
			obligationEl.select(obligationKey, true);
		}
		obligationEl.setVisible(ctrlConfig.isObligationVisible());
		
		Date startDate = moduleConfigs.getDateValue(CONFIG_KEY_START);
		startDateEl = uifactory.addDateChooser("config.start.date", startDate, formLayout);
		startDateEl.setDateChooserTimeEnabled(true);
		
		KeyValues triggerKV = getTriggerKV();
		triggerEl = uifactory.addRadiosVertical("config.trigger", formLayout,
				triggerKV.keys(), triggerKV.values());
		triggerEl.addActionListener(FormEvent.ONCHANGE);
		String triggerKey = moduleConfigs.getStringValue(CONFIG_KEY_TRIGGER, CONFIG_DEFAULT_TRIGGER);
		if (Arrays.asList(triggerEl.getKeys()).contains(triggerKey)) {
			triggerEl.select(triggerKey, true);
		}
		
		String score = moduleConfigs.getStringValue(CONFIG_KEY_SCORE_CUT_VALUE);
		scoreCutEl = uifactory.addTextElement("config.score.cut", 100, score, formLayout);
		scoreCutEl.setMandatory(true);
		
		uifactory.addFormSubmitButton("save", formLayout);
		
		updateUI();
	}

	private KeyValues getTriggerKV() {
		KeyValues triggerKV = new KeyValues();
		if (ctrlConfig.isTriggerNodeVisited()) {
			triggerKV.add(entry(CONFIG_VALUE_TRIGGER_NODE_VISITED, translate("config.trigger.visited")));
		}
		if (ctrlConfig.isTriggerConfirmed()) {
			triggerKV.add(entry(CONFIG_VALUE_TRIGGER_CONFIRMED, translate("config.trigger.confirmed")));
		}
		if (ctrlConfig.isTriggerScore()) {
			triggerKV.add(entry(CONFIG_VALUE_TRIGGER_SCORE, translate("config.trigger.score")));
		}
		if (ctrlConfig.isTriggerPassed()) {
			triggerKV.add(entry(CONFIG_VALUE_TRIGGER_PASSED, translate("config.trigger.passed")));
		}
		TranslateableBoolean triggerStatusInReview = ctrlConfig.getTriggerStatusInReview();
		if (triggerStatusInReview.isTrue()) {
			triggerKV.add(entry(CONFIG_VALUE_TRIGGER_STATUS_IN_REVIEW,
					getTranslationOrDefault(triggerStatusInReview, "config.trigger.status.in.review")));
		}
		TranslateableBoolean triggerStatusDone = ctrlConfig.getTriggerStatusDone();
		if (triggerStatusDone.isTrue()) {
			triggerKV.add(entry(CONFIG_VALUE_TRIGGER_STATUS_DONE,
					getTranslationOrDefault(triggerStatusDone, "config.trigger.status.done")));
		}
		return triggerKV;
	}
	
	private String getTranslationOrDefault(TranslateableBoolean trans, String defaulI18nKey) {
		return trans.isTranslated()
				? trans.getMessage()
				: translate(defaulI18nKey);
	}
	
	private void updateUI() {
		durationEl.setMandatory(isDurationMandatory());
		
		boolean triggerScore = triggerEl.isOneSelected() && triggerEl.getSelectedKey().equals(CONFIG_VALUE_TRIGGER_SCORE);
		scoreCutEl.setVisible(triggerScore);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == obligationEl) {
			updateUI();
		} else if (source == triggerEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		allOk = validateInteger(durationEl, 1, 10000, isDurationMandatory(), "error.positiv.int");
		allOk = validateInteger(scoreCutEl, 0, 10000, true, "error.positiv.int");
		
		return allOk & super.validateFormLogic(ureq);
	}
	
	public static boolean validateInteger(TextElement el, int min, int max, boolean mandatory, String i18nKey) {
		boolean allOk = true;
		el.clearError();
		if(el.isEnabled() && el.isVisible()) {
			String val = el.getValue();
			if(StringHelper.containsNonWhitespace(val)) {
				try {
					int value = Integer.parseInt(val);
					if(min > value) {
						allOk = false;
					} else if(max < value) {
						allOk = false;
					}
				} catch (NumberFormatException e) {
					allOk = false;
				}
			} else if (mandatory) {
				allOk = false;
			}
		}
		if (!allOk) {
			el.setErrorKey(i18nKey, null);
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String estimatedTime = durationEl.getValue();
		moduleConfigs.setStringValue(CONFIG_KEY_DURATION, estimatedTime);
		
		String obligation = obligationEl.isOneSelected()
				? obligationEl.getSelectedKey()
				: CONFIG_DEFAULT_OBLIGATION;
		moduleConfigs.setStringValue(CONFIG_KEY_OBLIGATION, obligation);
		
		Date startDate = startDateEl.getDate();
		moduleConfigs.setDateValue(CONFIG_KEY_START, startDate);
		
		String trigger = triggerEl.isOneSelected()
				? triggerEl.getSelectedKey()
				: CONFIG_DEFAULT_TRIGGER;
		moduleConfigs.setStringValue(CONFIG_KEY_TRIGGER, trigger);
		
		if (scoreCutEl.isVisible()) {
			String scoreCut = scoreCutEl.getValue();
			moduleConfigs.setStringValue(CONFIG_KEY_SCORE_CUT_VALUE, scoreCut);
		} else {
			moduleConfigs.remove(CONFIG_KEY_SCORE_CUT_VALUE);
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private boolean isDurationMandatory() {
		return CompletionType.duration.equals(courseConfig.getCompletionType())
				&& obligationEl.isOneSelected()
				&& AssessmentObligation.mandatory.name().equals(obligationEl.getSelectedKey());
	}
	
	public interface LearningPathControllerConfig {
		
		public boolean isObligationVisible();
		
		public boolean isTriggerNodeVisited();
		
		public boolean isTriggerConfirmed();
		
		public boolean isTriggerScore();
		
		public boolean isTriggerPassed();
		
		public TranslateableBoolean getTriggerStatusInReview();
		
		public TranslateableBoolean getTriggerStatusDone();
		
	}
	
	public static ControllerConfigBuilder builder() {
		return new ControllerConfigBuilder();
	}
	
	public static class ControllerConfigBuilder {
		
		public boolean obligationVisible = true;
		private boolean triggerNodeVisited;
		private boolean triggerConfirmed;
		private boolean triggerScore;
		private boolean triggerPassed;
		private TranslateableBoolean triggerStatusInReview;
		private TranslateableBoolean triggerStatusDone;
		
		private ControllerConfigBuilder() {
		}
		
		public ControllerConfigBuilder disableObligation() {
			obligationVisible = false;
			return this;
		}
		
		public ControllerConfigBuilder enableNodeVisited() {
			triggerNodeVisited = true;
			return this;
		}
		
		public ControllerConfigBuilder enableConfirmed() {
			triggerConfirmed = true;
			return this;
		}
		
		public ControllerConfigBuilder enableScore() {
			triggerScore = true;
			return this;
		}
		
		public ControllerConfigBuilder enablePassed() {
			triggerPassed = true;
			return this;
		}
		
		public ControllerConfigBuilder enableStatusInReview() {
			triggerStatusInReview = TranslateableBoolean.untranslatedTrue();
			return this;
		}
		
		public ControllerConfigBuilder enableStatusInReview(String message) {
			triggerStatusInReview = TranslateableBoolean.translatedTrue(message);
			return this;
		}
		
		public ControllerConfigBuilder enableStatusDone() {
			triggerStatusDone = TranslateableBoolean.untranslatedTrue();
			return this;
		}
		
		public ControllerConfigBuilder enableStatusDone(String message) {
			triggerStatusDone = TranslateableBoolean.translatedTrue(message);
			return this;
		}
		
		public LearningPathControllerConfig build() {
			return new ControllerConfigImpl(this);
		}
		
		private final static class ControllerConfigImpl implements LearningPathControllerConfig {
			
			public final boolean obligationVisible;
			public final boolean triggerNodeVisited;
			public final boolean triggerConfirmed;
			public final boolean triggerScore;
			public final boolean triggerPassed;
			public final TranslateableBoolean triggerStatusInReview;
			public final TranslateableBoolean triggerStatusDone;

			public ControllerConfigImpl(ControllerConfigBuilder builder) {
				this.obligationVisible = builder.obligationVisible;
				this.triggerNodeVisited = builder.triggerNodeVisited;
				this.triggerConfirmed = builder.triggerConfirmed;
				this.triggerScore = builder.triggerScore;
				this.triggerPassed = builder.triggerPassed;
				this.triggerStatusInReview = falseIfNull(builder.triggerStatusInReview);
				this.triggerStatusDone = falseIfNull(builder.triggerStatusDone);
			}
			
			private TranslateableBoolean falseIfNull(TranslateableBoolean translateableBoolean) {
				return translateableBoolean != null
						? translateableBoolean
						: TranslateableBoolean.untranslatedFalse();
			}

			@Override
			public boolean isObligationVisible() {
				return obligationVisible;
			}

			@Override
			public boolean isTriggerNodeVisited() {
				return triggerNodeVisited;
			}

			@Override
			public boolean isTriggerConfirmed() {
				return triggerConfirmed;
			}

			@Override
			public boolean isTriggerScore() {
				return triggerScore;
			}

			@Override
			public boolean isTriggerPassed() {
				return triggerPassed;
			}

			@Override
			public TranslateableBoolean getTriggerStatusInReview() {
				return triggerStatusInReview;
			}

			@Override
			public TranslateableBoolean getTriggerStatusDone() {
				return triggerStatusDone;
			}
			
		}
		
	}
	
}

