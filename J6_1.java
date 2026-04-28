interface Light{
	String[] str= {"Walk","Stop","Caution"};
	void print();
	
}

class Green implements Light{
	public void print() {
		System.out.println("Green is "+str[0]);
	}
}

class Red implements Light{
	public void print() {
		System.out.println("Red is "+str[1]);
	}
}

class Yellow implements Light{
	public void print() {
		System.out.println("Yellow is "+str[2]);
	}
}

public class J6_1 {

	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		
		Green G=new Green();
		Red R=new Red();
		Yellow Y=new Yellow();
		G.print();
		R.print();
		Y.print();
		
		/*
		Light G;
		G=new Green();
		Light R;
		R=new Red();
		Light Y;
		Y=new Yellow();
		G.print();
		R.print();
		Y.print();
		*/
	}

}
