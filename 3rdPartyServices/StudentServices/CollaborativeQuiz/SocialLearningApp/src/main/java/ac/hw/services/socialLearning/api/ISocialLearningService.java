package ac.hw.services.socialLearning.api;

import java.util.List;

public interface ISocialLearningService {
	
	String getServerIPPort();
	List<String> getUserInterests();

}