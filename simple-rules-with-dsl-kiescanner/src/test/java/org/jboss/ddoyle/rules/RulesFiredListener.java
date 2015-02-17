package org.jboss.ddoyle.rules;

import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.DefaultAgendaEventListener;

public class RulesFiredListener extends DefaultAgendaEventListener {

        private long nrOfRulesFired = 0;

        @Override
        public synchronized void afterMatchFired(AfterMatchFiredEvent event) {
                // TODO Auto-generated method stub
                nrOfRulesFired++;
        }

        public long getNrOfRulesFired() {
                return nrOfRulesFired;
        }

        public void setNrOfRulesFired(long nrOfRulesFired) {
                this.nrOfRulesFired = nrOfRulesFired;
        }

}