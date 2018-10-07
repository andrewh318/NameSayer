package app;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class Playlist {
    private ObservableList<Name> _playlist = FXCollections.observableArrayList();
    private String _playlistName;
    private int _position;

    public Playlist(String name){
        _playlistName = name;
    }
    public ObservableList<Name> getPlaylist(){
        return _playlist;
    }

    public void shufflePlaylist(){
        FXCollections.shuffle(_playlist);
    }

    @Override
    public String toString(){
        return _playlistName;
    }

    public String getPlaylistItemAt(int index){
        return _playlist.get(index).toString();
    }

    public void addName(Name name){
        if (name != null){
            _playlist.add(name);
        }
    }

    public void deleteName(Name name){
        if (name != null){
            _playlist.remove(name);
        }
    }

    public int getNumberOfItems(){
        return _playlist.size();
    }

    public void setPlaylistName(String name){
        _playlistName = name;
    }

    public String getName() {
        return _playlistName;
    }

    public ReadOnlyStringProperty nameProperty(){
        return new SimpleStringProperty(_playlistName);
    }

}
