package org.drools.brms.client.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;

/**
 * A sortable table widget. Extends the GWT Grid widget.
 * (more performant then FlexTable).
 */
public class SortableTable extends Grid implements TableListener {
	
    
    /** The stylez */
    public static String styleListHeader = "rule-ListHeader";
    public static String styleSelectedRow = "rule-SelectedRow";
    public static String styleEvenRow = "rule-ListEvenRow";
    public static String styleList = "rule-List";
    
    
    private static int SORT_ASCENDING = 0;
    private static int SORT_DESCENDING = 1;    
    
    /** vars for current storting state */
	private int sortColIndex		=	-1;
	private int sortDirection		=	-1;

    /** Icons for sorting status */
	private String ascendingIcon 	= "images/shuffle_up.gif";
	private String descendingIcon	= "images/shuffle_down.gif";
	private String blankImage		= "images/up_down.gif";

	// Holds the data rows of the table
	// This is a list of RowData Object
	private List tableRows 			= new ArrayList();
	
	// Holds the data for the column headers
	private List tableHeader 		= new ArrayList();
    private int hideColumnIndex;
    private int selectedRow;

    
	/** 
     * Create a sortable table widget, of the specified proportions.
     * The number of rows does NOT include the header. 
	 */
	public SortableTable(int rows, int cols){       
		super(rows + 1, cols);		
        
		this.addTableListener(this);
        setStyleName( styleList );
        
	}

	/** 
     * Adds a header, which will be at the zero index in the table.
	 */
	public void addColumnHeader(String name, int index){
               
		tableHeader.add(index, name);
		this.renderTableHeader(name, index);
	}

    
    /** 
     * This can be used to ensure that a column is invisible.
     * This will also include the header (first row)
     * You would use this to allow a "key" column to be stored with the data.
     * For example, a UUID for a rule. 
     */
    public void setHiddenColumn(int colIndex) {
        this.hideColumnIndex = colIndex;
        this.getCellFormatter().setVisible( 0, colIndex, false );
    }
    
	/**
     * This will store the value in the x,y position.
     * Values must be comparable for sorting to work of course.
	 */
	public void setValue(int rowIndex, int colIndex, Comparable value){
		// The rowIndex should begin with 1 as rowIndex 0 is for the Header
		// Any row with index == 0 will not be displayed.
        
		if(rowIndex == 0){
			return;
		}
        
        //and do the zebra striping
        if (rowIndex % 2 == 0) {           
            getCellFormatter().setStyleName( rowIndex, colIndex, styleEvenRow  );
        }
        
		if((rowIndex-1) >= this.tableRows.size() || null == tableRows.get(rowIndex-1)){
			tableRows.add(rowIndex-1, new RowData());
		}
		
		RowData rowData = (RowData)this.tableRows.get(rowIndex-1); 
		rowData.addColumnValue(colIndex, value);
		this.setHTML(rowIndex, colIndex, "" + value.toString()+ "");
        
        //and hiding the required column
        if (colIndex == hideColumnIndex) {
            getCellFormatter().setVisible( rowIndex, colIndex, false );
        }
	}
    

    /** This performs the sorting */
	public void sort(int columnIndex){
		Collections.sort(this.tableRows);
		if(this.sortColIndex != columnIndex){
			// New Column Header clicked
			// Reset the sortDirection to ASC
			this.sortDirection = SORT_ASCENDING;
		}else{
			// Reverse the sortDirection
			this.sortDirection = (this.sortDirection == SORT_ASCENDING)? SORT_DESCENDING:SORT_ASCENDING; 
		}
		this.sortColIndex = columnIndex;
	}
	
	/** 
     * When a cell is clicked, the selected row is styled, and 
     * the currently selected row is remembered.
     * 
     * If it was in-fact a header that was clicked, then it will sort the 
     * data and redisplay the grid.
	 */
	public void onCellClicked(SourcesTableEvents sender, int row, int col) {

        styleSelectedRow(row);
		clickSort( row, col );
	}
    
    /**
     * This will apply the "highlight" for the selected row, and remove it from the previous
     * one, and set the selectedRow.
     */
    private void styleSelectedRow(int row) {
        if (row != 0) {
            getRowFormatter().addStyleName( row , styleSelectedRow );
            getRowFormatter().removeStyleName( selectedRow, styleSelectedRow );
            selectedRow = row;
        }
    }
    
    /**
     * @return The selected row index.
     */
    public int getSelectedRow() {
        return this.selectedRow;
    }
         
    /**
     * This actually kicks off the sorting.
     */
    private void clickSort(int row,
                           int col) {
        if(row != 0){
			return;
		}
		this.setSortColIndex(col);
		this.sort(col);
		this.drawTable();
    }	

	/*
	 * getSortAscImage
	 * 
	 * Getter for Sort Ascending Image
	 * 
	 * @return String
	 */
	public String getSortAscImage() {
		return ascendingIcon;
	}

	/*
	 * setSortAscImage
	 * 
	 * Setter for Sort Ascending Image
	 * 
	 * @param relative path + image name (String)
	 * e.g. images/asc.gif
	 */
	public void setSortAscImage(String sortAscImage) {
		this.ascendingIcon = sortAscImage;
	}

	/*
	 * getSortDescImage
	 * 
	 * Getter for Sort Descending Image
	 * 
	 * @return String
	 */
	public String getSortDescImage() {
		return descendingIcon;
	}

	/*
	 * setSortDescImgage
	 * 
	 * Setter for Sort Descending Image
	 * 
	 * @param relative path + image name (String)
	 * e.g. images/desc.gif
	 */
	public void setSortDescImgage(String sortDescImgage) {
		this.descendingIcon = sortDescImgage;
	}
	
	/*
	 * getBlankImage
	 * 
	 * Getter for blank Image
	 * 
	 * @return String
	 */
	public String getBlankImage() {
		return blankImage;
	}

	/*
	 * setBlankImage
	 * 
	 * Setter for the blank Image
	 * 
	 * @param relative path + image name (String)
	 * e.g. images/blank.gif
	 */
	public void setBlankImage(String blankImage) {
		this.blankImage = blankImage;
	}
	
	/*
	 * drawTable
	 * 
	 * Renders the header as well as the body 
	 * of the table
	 */
	protected void drawTable(){
		this.displayTableHeader();
		this.displayTableBody();
	}
	
	/*
	 * displayTableHeader
	 * 
	 * Renders only the table header
	 */
	private void displayTableHeader(){
		int colIndex=0;
		for(Iterator colHeaderIter = this.tableHeader.iterator(); colHeaderIter.hasNext();){
			String colHeader = (String)colHeaderIter.next();
			this.renderTableHeader(colHeader, colIndex++);
		}
        
    }
	
	/*
	 * displayTableBody
	 * 
	 * Renders the body or the remaining rows of the table
	 * except the header.
	 * It checks the sort direction and displays the rows 
	 * accordingly
	 */
	private void displayTableBody(){
		if(this.sortDirection == SORT_ASCENDING || this.sortDirection == -1){
			// Ascending order and Default Display
			for(int rowIndex=0; rowIndex<tableRows.size(); rowIndex++){
				RowData columns = (RowData)tableRows.get(rowIndex);
				for(int colIndex=0; colIndex<columns.getColumnValues().size(); colIndex++){
					Object value = columns.getColumnValue(colIndex);
					if(null != value){
						this.setHTML(rowIndex+1, colIndex, value.toString());
					}
				}
			}
		}else{
			// Descending Order Display
			for(int rowIndex=tableRows.size()-1, rowNum = 1; rowIndex>=0; rowIndex--, rowNum++){
				RowData columns = (RowData)tableRows.get(rowIndex);
				for(int colIndex=0; colIndex<columns.getColumnValues().size(); colIndex++){
					Object value = columns.getColumnValue(colIndex);
					if(null != value){
						this.setHTML(rowNum, colIndex, value.toString());
					}
				}
			}
		}
	}
	
	/*
	 * setSortColIndex
	 * 
	 * Sets the current column index being sorted
	 * 
	 * @param column index being sorted (int)
	 */
	private void setSortColIndex(int sortIndex){
		for(int rowIndex=0; rowIndex<tableRows.size(); rowIndex++){
			RowData row = (RowData)tableRows.get(rowIndex);
			row.setSortColIndex(sortIndex);
		}
	}
	
	/*
	 * renderTableHeader
	 * Renders a particular column in the Table Header
	 * 
	 * @param Column Name (String)
	 * @param Column Index (int) 
	 */
	private void renderTableHeader(String name, int index){
		StringBuffer headerText = new StringBuffer();
		headerText.append(name);
		headerText.append("&nbsp;<img border='0' src=");
		if(this.sortColIndex == index){
			if(this.sortDirection == SORT_ASCENDING){
				headerText.append("'" + this.ascendingIcon + "' alt='Ascending' ");	
			}else{
				headerText.append("'" + this.descendingIcon + "' alt='Descending' ");
			}
		}else{
			headerText.append("'" + this.blankImage + "'");
		}
		headerText.append("/>");

		this.setHTML(0, index, headerText.toString());
        getRowFormatter().setStyleName( 0, styleListHeader );
    
	}
}
