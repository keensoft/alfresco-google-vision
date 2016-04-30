package es.keensoft.alfresco.google;

public class SafeSearchConfig {
	
	private Likelihood adultLikelihoodLevel;
	private Likelihood medicalLikelihoodLevel;
	private Likelihood violenceLikelihoodLevel;
	private Likelihood spoofLikelihoodLevel;
	
	public enum Likelihood {
		
        UNKNOWN,
        VERY_UNLIKELY,
        UNLIKELY,
        POSSIBLE,
        LIKELY,
        VERY_LIKELY;
		
		public static Boolean isLikelyOrBetter(Likelihood l1, Likelihood l2) {
			if (l1 == l2) {
				return true;
			}
			if (l1 == VERY_UNLIKELY) {
				return false;
			}
			if (l1 == UNLIKELY) {
				return l2 == VERY_UNLIKELY;
			}
			if (l1 == POSSIBLE) {
				return (l2 == VERY_UNLIKELY || l2 == UNLIKELY);
			}
			if (l1 == LIKELY) {
				return (l2 == POSSIBLE || l2 == VERY_UNLIKELY || l2 == UNLIKELY);
			}
			if (l1 == VERY_LIKELY) {
				return (l2 == LIKELY || l2 == POSSIBLE || l2 == VERY_UNLIKELY || l2 == UNLIKELY);
			}
			return false;
		}
		
	}
	
	public Boolean getEnabled() {
		return adultLikelihoodLevel != null || 
			   medicalLikelihoodLevel != null || 
			   violenceLikelihoodLevel != null ||
			   spoofLikelihoodLevel != null;
	}
	
	public Likelihood getAdultLikelihoodLevel() {
		return adultLikelihoodLevel;
	}
	public void setAdultLikelihoodLevel(Likelihood adultLikelihoodLevel) {
		this.adultLikelihoodLevel = adultLikelihoodLevel;
	}
	public Likelihood getMedicalLikelihoodLevel() {
		return medicalLikelihoodLevel;
	}
	public void setMedicalLikelihoodLevel(Likelihood medicalLikelihoodLevel) {
		this.medicalLikelihoodLevel = medicalLikelihoodLevel;
	}
	public Likelihood getViolenceLikelihoodLevel() {
		return violenceLikelihoodLevel;
	}
	public void setViolenceLikelihoodLevel(Likelihood violenceLikelihoodLevel) {
		this.violenceLikelihoodLevel = violenceLikelihoodLevel;
	}
	public Likelihood getSpoofLikelihoodLevel() {
		return spoofLikelihoodLevel;
	}
	public void setSpoofLikelihoodLevel(Likelihood spoofLikelihoodLevel) {
		this.spoofLikelihoodLevel = spoofLikelihoodLevel;
	}

}
