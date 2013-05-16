package org.sparql;

public class LinkTriple implements Comparable<LinkTriple> {

	public LinkTriple(SparqlData subj, SparqlData pred, SparqlData obj) {
		this.subj = subj;
		this.pred = pred;
		this.obj = obj;
	}
	public SparqlData subj, pred, obj;

	@Override
	public String toString() {
		return "<" + subj + "> <" + pred + "> <" + obj + ">";		
	}	

	@Override
	public int compareTo(LinkTriple other) {
		int diff1 = 0, diff2 = 0, diff3 = 0;
		if ( subj != other.subj ) {
			if ( subj != null )
				diff1 = subj.compareTo(other.subj);		
			else 
				diff1 = 1;
		}
		if ( pred != other.pred ) {
			if ( pred != null )
				diff2 = pred.compareTo(other.pred);					
			else
				diff2 = 1;
		}
		if ( obj != other.obj ) {
			if ( obj != null )
				diff3 = obj.compareTo(other.obj);			
			else
				diff3 = 1;
		}
		if ( diff1 != 0 ) 
			return diff1;
		if ( diff2 != 0 ) 
			return diff2;
		if ( diff3 != 0 ) 
			return diff3;
		return 0;		
	}

	@Override
	public int hashCode() {
		return subj.hashCode() + pred.hashCode() + obj.hashCode();
	}

	public boolean equals(Object o) {
		if (o instanceof LinkTriple) {
			LinkTriple other = (LinkTriple) o;
			return (subj == other.subj && pred == other.pred && obj == other.obj);
		}
		return false;
	}
}
