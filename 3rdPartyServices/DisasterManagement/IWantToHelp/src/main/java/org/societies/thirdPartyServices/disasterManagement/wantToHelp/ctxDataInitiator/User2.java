package org.societies.thirdPartyServices.disasterManagement.wantToHelp.ctxDataInitiator;

public class User2 extends BaseUser{

	@Override
	public String getName() {
		
		return "User2";
	}

	@Override
	public String getSex() {
		
		return "female";
	}

	@Override
	public String getAge() {
		
		return "29";
	}

	@Override
	public String getLanguages() {

		return null;
	}

	@Override
	public String getInterests() {
	
		return "";
	}

	@Override
	public String getMovies() {

		return "";
	}

	@Override
	public String getOccupation() {

		return "employed";
	}

	@Override
	public String getStatus() {
		
		return "busy";
	}

	@Override
	public String getEmail() {
		
		return null;
	}

	@Override
	public String getBirthday() {
		
		return "15/3/1971";
	}

	@Override
	public String getPoliticalViews() {
		
		return null;
	}

	@Override
	public String getLocationSymbolic() {

		return null;
	}

	@Override
	public String getLocationCoordinates() {
		
		return null;
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
		return BaseUser.SKILLS[4];
	}
	
}
