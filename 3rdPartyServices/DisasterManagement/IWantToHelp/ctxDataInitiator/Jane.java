package org.societies.thirdPartyServices.disasterManagement.wantToHelp.ctxDataInitiator;

public class Jane extends BaseUser{

	@Override
	public String getName() {
		
		return "Jane";
	}

	@Override
	public String getSex() {
		
		return "female";
	}

	@Override
	public String getAge() {
		
		return "28";
	}

	@Override
	public String getLanguages() {
		
		return "english,french,greek";
	}

	@Override
	public String getInterests() {
	
		return "cooking,reading,music";
	}

	@Override
	public String getMovies() {

		return "batman,superman";
	}

	@Override
	public String getOccupation() {

		return "unemployed";
	}

	@Override
	public String getStatus() {
		
		return "free";
	}

	@Override
	public String getEmail() {
		
		return "john@societies.org";
	}

	@Override
	public String getBirthday() {
		
		return "15/6/1981";
	}

	@Override
	public String getPoliticalViews() {
		
		return "liberal";
	}

	@Override
	public String getLocationSymbolic() {
		
		return "HWLectureTheater1";
	}

	@Override
	public String getLocationCoordinates() {
		
		return "1234,1234";
	}

	@Override
	public String getFriends() {
		
		return "";
	}

	/* (non-Javadoc)
	 * @see org.societies.context.dataInit.impl.BaseUser#getSkills()
	 */
	@Override
	public String getSkills() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
