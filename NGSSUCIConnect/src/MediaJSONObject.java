import java.util.Comparator;

public class MediaJSONObject implements Comparator<MediaJSONObject> {
	public String id = "";
	public String caption = "";
	public String media_source_image = "";
	public String media_source_audio = "";
	public String condition = "";
	public double weightCondition = 0.15;
	public double distanceFromUser = 0.0;
	public double weightLatest = 0.0;
	public double cost = 0.0;
	public static double totalWeightLatest = 0.0;
	public static double totalCost = 0.0;
	public static double totalDistance = 0.0;
	public double finalWeight = -1.0;

	@Override
	public int compare(MediaJSONObject o1, MediaJSONObject o2) {
		if (o1.finalWeight < 0.0)
			o1.finalWeight = ((o1.cost / totalCost) + (o1.weightLatest / totalWeightLatest) + (o1.distanceFromUser / totalDistance) + o1.weightCondition);

		if (o2.finalWeight < 0.0)
			o2.finalWeight = ((o2.cost / totalCost) + (o2.weightLatest / totalWeightLatest) + (o2.distanceFromUser / totalDistance) + o2.weightCondition);

		double ans = (o1.finalWeight) - (o2.finalWeight);
		if (ans == 0)
			return 0;
		return ans > 0 ? 1 : -1;
	}
}
