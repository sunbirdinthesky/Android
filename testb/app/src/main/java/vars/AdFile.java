package vars;

import java.util.Vector;
public class AdFile implements Cloneable {
	public String name;
	public int playCnt = 0;
	public String md5;
	public String begin;
	public String end;
	public String company;
	public Vector<UploadIndex> Config = new Vector<>();
	
	@SuppressWarnings("unchecked")
	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		AdFile tmp = (AdFile)super.clone();
		tmp.name = name;
		tmp.playCnt = playCnt;
		tmp.md5 = md5;
		tmp.begin = begin;
		tmp.end = end;
		tmp.company = company;
		tmp.Config = (Vector<UploadIndex>) Config.clone();
		return tmp;
	}
}